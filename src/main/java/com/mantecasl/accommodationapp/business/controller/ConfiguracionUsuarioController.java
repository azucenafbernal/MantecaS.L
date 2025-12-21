package com.mantecasl.accommodationapp.business.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mantecasl.accommodationapp.business.entity.Favorito;
import com.mantecasl.accommodationapp.business.entity.Propietario;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.FavoritoDAO;
import com.mantecasl.accommodationapp.business.persistance.PropietarioDAO;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionUsuarioController {

    private static final String ATTR_ERROR = "error";
    private static final String ATTR_USUARIO = "usuario";
    private static final String ATTR_PROPIETARIO = "propietario";
    private static final String ATTR_ERROR_TARJETA = "errorTarjeta";
    private static final String ATTR_ERROR_PASSWORD = "errorPassword";
    private static final String VIEW_CONFIG = "configuracionusuario";
    private static final String MSG_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    private static final String MSG_NO_PROPIETARIO = "Este usuario no es propietario";

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionUsuarioController.class);

    private final UsuarioDAO usuarioDAO;
    private final PropietarioDAO propietarioDAO;
    private final FavoritoDAO favoritoDAO;

    public ConfiguracionUsuarioController(UsuarioDAO usuarioDAO, PropietarioDAO propietarioDAO, FavoritoDAO favoritoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.propietarioDAO = propietarioDAO;
        this.favoritoDAO = favoritoDAO;
    }

    /** Muestra la página de configuración */
    @GetMapping
    public String mostrarConfiguracion(@RequestParam Long idUsuario, Model model) {
        try {
            Usuario usuario = usuarioDAO.findById(idUsuario).orElse(null);

            if (usuario == null) {
                model.addAttribute(ATTR_ERROR, MSG_USUARIO_NO_ENCONTRADO);
                return ATTR_ERROR;
            }

            // Comprobar si es propietario
            Propietario propietario = propietarioDAO.findByUsuarioId(idUsuario);

            model.addAttribute(ATTR_USUARIO, usuario);
            model.addAttribute(ATTR_PROPIETARIO, propietario);

            return VIEW_CONFIG; // Thymeleaf
        } catch (Exception e) {
            model.addAttribute(ATTR_ERROR, "Error al cargar la configuración: " + e.getMessage());
            return ATTR_ERROR;
        }
    }

    /** Actualizar datos básicos */
    @PostMapping("/actualizarUsuario")
    public String actualizarUsuario(
            @ModelAttribute Usuario usuarioActualizado,
            Model model) {

        Usuario usuario = usuarioDAO.findById(usuarioActualizado.getId()).orElse(null);

        if (usuario == null) {
            model.addAttribute(ATTR_ERROR, MSG_USUARIO_NO_ENCONTRADO);
            return ATTR_ERROR;
        }

        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setEmail(usuarioActualizado.getEmail());
        usuario.setContrasena(usuarioActualizado.getContrasena());

        usuarioDAO.save(usuario);

        return "redirect:/configuracion?idUsuario=" + usuario.getId();
    }

    /** Actualizar datos del Propietario - Solo teléfono */
    @PostMapping("/actualizarPropietario")
    public String actualizarPropietario(
            @RequestParam Long usuarioId,
            @RequestParam String telefono,
            Model model) {

        Propietario p = propietarioDAO.findByUsuarioId(usuarioId);

        if (p == null) {
            model.addAttribute(ATTR_ERROR, MSG_NO_PROPIETARIO);
            return ATTR_ERROR;
        }

        p.setTelefonoContacto(telefono);

        propietarioDAO.save(p);

        model.addAttribute(ATTR_USUARIO, usuarioDAO.findById(usuarioId).orElse(null));
        model.addAttribute(ATTR_PROPIETARIO, p);
        model.addAttribute("successTelefono", "Teléfono actualizado correctamente");
        return VIEW_CONFIG;
    }

    /** Actualizar cuenta bancaria */
    @PostMapping("/actualizarCuentaBancaria")
    public String actualizarCuentaBancaria(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String cuenta,
            Model model) {

        Propietario p = propietarioDAO.findByUsuarioId(usuarioId);

        if (p == null) {
            model.addAttribute(ATTR_ERROR, MSG_NO_PROPIETARIO);
            return ATTR_ERROR;
        }

        // Validar que la cuenta no esté vacía
        if (cuenta == null || cuenta.trim().isEmpty()) {
            model.addAttribute(ATTR_USUARIO, usuarioDAO.findById(usuarioId).orElse(null));
            model.addAttribute(ATTR_PROPIETARIO, p);
            model.addAttribute("errorBanco", "El número de cuenta bancaria no puede estar vacío");
            return VIEW_CONFIG;
        }

        // Validación básica de IBAN (al menos 15 caracteres)
        if (cuenta.trim().length() < 15) {
            model.addAttribute(ATTR_USUARIO, usuarioDAO.findById(usuarioId).orElse(null));
            model.addAttribute(ATTR_PROPIETARIO, p);
            model.addAttribute("errorBanco", "El IBAN debe tener al menos 15 caracteres");
            return VIEW_CONFIG;
        }

        p.setCuentaBancaria(cuenta);
        propietarioDAO.save(p);

        model.addAttribute(ATTR_USUARIO, usuarioDAO.findById(usuarioId).orElse(null));
        model.addAttribute(ATTR_PROPIETARIO, p);
        model.addAttribute("successBanco", "Cuenta bancaria actualizada correctamente");
        return VIEW_CONFIG;
    }

    /** Actualizar tarjeta de crédito */
    @PostMapping("/actualizarTarjeta")
    public String actualizarTarjeta(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String creditcard,
            @RequestParam(required = false) String expiry,
            @RequestParam(required = false) String cvv,
            Model model) {

        Propietario p = propietarioDAO.findByUsuarioId(usuarioId);

        if (p == null) {
            model.addAttribute(ATTR_ERROR, MSG_NO_PROPIETARIO);
            return ATTR_ERROR;
        }

        // Validar número de tarjeta
        if (creditcard != null && !creditcard.isEmpty()) {
            // Remover espacios para validación
            String cardNumberOnly = creditcard.replaceAll("\\s+", "");
            if (cardNumberOnly.length() < 13 || cardNumberOnly.length() > 19) {
                model.addAttribute(ATTR_USUARIO, usuarioDAO.findById(usuarioId).orElse(null));
                model.addAttribute(ATTR_PROPIETARIO, p);
                model.addAttribute(ATTR_ERROR_TARJETA, "El número de tarjeta debe tener entre 13 y 19 dígitos");
                return VIEW_CONFIG;
            }
            p.setNumeroTarjeta(creditcard);
        }

        // Validar fecha de vencimiento (MM/YY)
        if (expiry != null && !expiry.isEmpty()) {
            if (!expiry.matches("\\d{2}/\\d{2}")) {
                model.addAttribute(ATTR_USUARIO, usuarioDAO.findById(usuarioId).orElse(null));
                model.addAttribute(ATTR_PROPIETARIO, p);
                model.addAttribute(ATTR_ERROR_TARJETA, "La fecha de vencimiento debe estar en formato MM/YY");
                return VIEW_CONFIG;
            }
            p.setFechaVencimiento(expiry);
        }

        // Validar CVV
        if (cvv != null && !cvv.isEmpty()) {
            if (!cvv.matches("\\d{3,4}")) {
                model.addAttribute(ATTR_USUARIO, usuarioDAO.findById(usuarioId).orElse(null));
                model.addAttribute(ATTR_PROPIETARIO, p);
                model.addAttribute(ATTR_ERROR_TARJETA, "El CVV debe tener 3 o 4 dígitos");
                return VIEW_CONFIG;
            }
            p.setCvv(cvv);
        }

        propietarioDAO.save(p);

        model.addAttribute(ATTR_USUARIO, usuarioDAO.findById(usuarioId).orElse(null));
        model.addAttribute(ATTR_PROPIETARIO, p);
        model.addAttribute("successTarjeta", "Tarjeta de crédito actualizada correctamente");
        return VIEW_CONFIG;
    }

    /** Cambiar contraseña */
    @PostMapping("/cambiarContrasena")
    public String cambiarContrasena(
            @RequestParam Long usuarioId,
            @RequestParam String passwordActual,
            @RequestParam String passwordNueva,
            @RequestParam String passwordConfirm,
            Model model) {

        Usuario usuario = usuarioDAO.findById(usuarioId).orElse(null);

        if (usuario == null) {
            model.addAttribute(ATTR_ERROR, MSG_USUARIO_NO_ENCONTRADO);
            return ATTR_ERROR;
        }

        // Verificar que la contraseña actual es correcta
        if (!usuario.getContrasena().equals(passwordActual)) {
            model.addAttribute(ATTR_USUARIO, usuario);
            model.addAttribute(ATTR_PROPIETARIO, propietarioDAO.findByUsuarioId(usuarioId));
            model.addAttribute(ATTR_ERROR_PASSWORD, "La contraseña actual es incorrecta");
            return VIEW_CONFIG;
        }

        // Verificar que las nuevas contraseñas coinciden
        if (!passwordNueva.equals(passwordConfirm)) {
            model.addAttribute(ATTR_USUARIO, usuario);
            model.addAttribute(ATTR_PROPIETARIO, propietarioDAO.findByUsuarioId(usuarioId));
            model.addAttribute(ATTR_ERROR_PASSWORD, "Las nuevas contraseñas no coinciden");
            return VIEW_CONFIG;
        }

        // Validar que la nueva contraseña no sea vacía
        if (passwordNueva.trim().isEmpty()) {
            model.addAttribute(ATTR_USUARIO, usuario);
            model.addAttribute(ATTR_PROPIETARIO, propietarioDAO.findByUsuarioId(usuarioId));
            model.addAttribute(ATTR_ERROR_PASSWORD, "La nueva contraseña no puede estar vacía");
            return VIEW_CONFIG;
        }

        // Actualizar la contraseña
        usuario.setContrasena(passwordNueva);
        usuarioDAO.save(usuario);

        model.addAttribute("successPassword", "Contraseña actualizada correctamente");
        model.addAttribute(ATTR_USUARIO, usuario);
        model.addAttribute(ATTR_PROPIETARIO, propietarioDAO.findByUsuarioId(usuarioId));
        return VIEW_CONFIG;
    }

    /** Eliminar cuenta */
    @PostMapping("/eliminarCuenta")
    public ResponseEntity<String> eliminarCuenta(@RequestParam Long idUsuario, HttpSession session) {
        try {
            log.info("Eliminando cuenta para usuario ID: {}", idUsuario);
            
            // 1. Eliminar favoritos del usuario
            List<Favorito> favoritos = favoritoDAO.findByUsuarioId(idUsuario);
            if (favoritos != null && !favoritos.isEmpty()) {
                log.info("Eliminando {} favoritos del usuario", favoritos.size());
                favoritoDAO.deleteAll(favoritos);
            }
            
            // 2. Eliminar propietario asociado (y sus inmuebles en cascada)
            Propietario propietario = propietarioDAO.findByUsuarioId(idUsuario);
            if (propietario != null) {
                log.info("Eliminando propietario ID: {}", propietario.getId());
                propietarioDAO.delete(propietario);
            }
            
            // 3. Eliminar usuario
            usuarioDAO.deleteById(idUsuario);
            session.invalidate();
            log.info("Cuenta eliminada correctamente");
            return ResponseEntity.ok("Cuenta eliminada correctamente");
        } catch (Exception e) {
            log.error("Error al eliminar cuenta: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error al eliminar la cuenta: " + e.getMessage());
        }
    }

    /** Cerrar sesión */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; 
    }
}


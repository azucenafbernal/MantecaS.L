package com.mantecasl.accommodationapp.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mantecasl.accommodationapp.business.entity.Propietario;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.PropietarioDAO;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionUsuarioController {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private PropietarioDAO propietarioDAO;

    /** Muestra la página de configuración */
    @GetMapping
    public String mostrarConfiguracion(@RequestParam Long idUsuario, Model model) {
        try {
            Usuario usuario = usuarioDAO.findById(idUsuario).orElse(null);

            if (usuario == null) {
                model.addAttribute("error", "Usuario no encontrado");
                return "error";
            }

            // Comprobar si es propietario
            Propietario propietario = propietarioDAO.findByUsuarioId(idUsuario);

            model.addAttribute("usuario", usuario);
            model.addAttribute("propietario", propietario);

            return "configuracionusuario"; // Thymeleaf
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la configuración: " + e.getMessage());
            return "error";
        }
    }

    /** Actualizar datos básicos */
    @PostMapping("/actualizarUsuario")
    public String actualizarUsuario(
            @ModelAttribute Usuario usuarioActualizado,
            Model model) {

        Usuario usuario = usuarioDAO.findById(usuarioActualizado.getId()).orElse(null);

        if (usuario == null) {
            model.addAttribute("error", "Usuario no encontrado");
            return "error";
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
            model.addAttribute("error", "Este usuario no es propietario");
            return "error";
        }

        p.setTelefonoContacto(telefono);

        propietarioDAO.save(p);

        model.addAttribute("usuario", usuarioDAO.findById(usuarioId).orElse(null));
        model.addAttribute("propietario", p);
        model.addAttribute("successTelefono", "Teléfono actualizado correctamente");
        return "configuracionusuario";
    }

    /** Actualizar cuenta bancaria */
    @PostMapping("/actualizarCuentaBancaria")
    public String actualizarCuentaBancaria(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String cuenta,
            Model model) {

        Propietario p = propietarioDAO.findByUsuarioId(usuarioId);

        if (p == null) {
            model.addAttribute("error", "Este usuario no es propietario");
            return "error";
        }

        // Validar que la cuenta no esté vacía
        if (cuenta == null || cuenta.trim().isEmpty()) {
            model.addAttribute("usuario", usuarioDAO.findById(usuarioId).orElse(null));
            model.addAttribute("propietario", p);
            model.addAttribute("errorBanco", "El número de cuenta bancaria no puede estar vacío");
            return "configuracionusuario";
        }

        // Validación básica de IBAN (al menos 15 caracteres)
        if (cuenta.trim().length() < 15) {
            model.addAttribute("usuario", usuarioDAO.findById(usuarioId).orElse(null));
            model.addAttribute("propietario", p);
            model.addAttribute("errorBanco", "El IBAN debe tener al menos 15 caracteres");
            return "configuracionusuario";
        }

        p.setCuentaBancaria(cuenta);
        propietarioDAO.save(p);

        model.addAttribute("usuario", usuarioDAO.findById(usuarioId).orElse(null));
        model.addAttribute("propietario", p);
        model.addAttribute("successBanco", "Cuenta bancaria actualizada correctamente");
        return "configuracionusuario";
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
            model.addAttribute("error", "Este usuario no es propietario");
            return "error";
        }

        // Validar número de tarjeta
        if (creditcard != null && !creditcard.isEmpty()) {
            // Remover espacios para validación
            String cardNumberOnly = creditcard.replaceAll("\\s+", "");
            if (cardNumberOnly.length() < 13 || cardNumberOnly.length() > 19) {
                model.addAttribute("usuario", usuarioDAO.findById(usuarioId).orElse(null));
                model.addAttribute("propietario", p);
                model.addAttribute("errorTarjeta", "El número de tarjeta debe tener entre 13 y 19 dígitos");
                return "configuracionusuario";
            }
            p.setNumeroTarjeta(creditcard);
        }

        // Validar fecha de vencimiento (MM/YY)
        if (expiry != null && !expiry.isEmpty()) {
            if (!expiry.matches("\\d{2}/\\d{2}")) {
                model.addAttribute("usuario", usuarioDAO.findById(usuarioId).orElse(null));
                model.addAttribute("propietario", p);
                model.addAttribute("errorTarjeta", "La fecha de vencimiento debe estar en formato MM/YY");
                return "configuracionusuario";
            }
            p.setFechaVencimiento(expiry);
        }

        // Validar CVV
        if (cvv != null && !cvv.isEmpty()) {
            if (!cvv.matches("\\d{3,4}")) {
                model.addAttribute("usuario", usuarioDAO.findById(usuarioId).orElse(null));
                model.addAttribute("propietario", p);
                model.addAttribute("errorTarjeta", "El CVV debe tener 3 o 4 dígitos");
                return "configuracionusuario";
            }
            p.setCvv(cvv);
        }

        propietarioDAO.save(p);

        model.addAttribute("usuario", usuarioDAO.findById(usuarioId).orElse(null));
        model.addAttribute("propietario", p);
        model.addAttribute("successTarjeta", "Tarjeta de crédito actualizada correctamente");
        return "configuracionusuario";
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
            model.addAttribute("error", "Usuario no encontrado");
            return "error";
        }

        // Verificar que la contraseña actual es correcta
        if (!usuario.getContrasena().equals(passwordActual)) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("propietario", propietarioDAO.findByUsuarioId(usuarioId));
            model.addAttribute("errorPassword", "La contraseña actual es incorrecta");
            return "configuracionusuario";
        }

        // Verificar que las nuevas contraseñas coinciden
        if (!passwordNueva.equals(passwordConfirm)) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("propietario", propietarioDAO.findByUsuarioId(usuarioId));
            model.addAttribute("errorPassword", "Las nuevas contraseñas no coinciden");
            return "configuracionusuario";
        }

        // Validar que la nueva contraseña no sea vacía
        if (passwordNueva.trim().isEmpty()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("propietario", propietarioDAO.findByUsuarioId(usuarioId));
            model.addAttribute("errorPassword", "La nueva contraseña no puede estar vacía");
            return "configuracionusuario";
        }

        // Actualizar la contraseña
        usuario.setContrasena(passwordNueva);
        usuarioDAO.save(usuario);

        model.addAttribute("successPassword", "Contraseña actualizada correctamente");
        model.addAttribute("usuario", usuario);
        model.addAttribute("propietario", propietarioDAO.findByUsuarioId(usuarioId));
        return "configuracionusuario";
    }

    /** Eliminar cuenta */
    @PostMapping("/eliminarCuenta")
    public String eliminarCuenta(@RequestParam Long idUsuario) {
        usuarioDAO.deleteById(idUsuario);
        return "cuenta_eliminada"; 
    }

    /** Cerrar sesión */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; 
    }
}


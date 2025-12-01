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

    /** Actualizar datos del Propietario */
    @PostMapping("/actualizarPropietario")
    public String actualizarPropietario(
            @RequestParam Long usuarioId,
            @RequestParam String telefono,
            @RequestParam String cuenta,
            @RequestParam(required = false) String creditcard,
            @RequestParam(required = false) String expiry,
            @RequestParam(required = false) String cvv,
            Model model) {

        Propietario p = propietarioDAO.findByUsuarioId(usuarioId);

        if (p == null) {
            model.addAttribute("error", "Este usuario no es propietario");
            return "error";
        }

        p.setTelefonoContacto(telefono);
        p.setCuentaBancaria(cuenta);
        
        // Actualizar campos de tarjeta de crédito si se proporcionan
        if (creditcard != null && !creditcard.isEmpty()) {
            p.setNumeroTarjeta(creditcard);
        }
        if (expiry != null && !expiry.isEmpty()) {
            p.setFechaVencimiento(expiry);
        }
        if (cvv != null && !cvv.isEmpty()) {
            p.setCvv(cvv);
        }

        propietarioDAO.save(p);

        return "redirect:/configuracion?idUsuario=" + usuarioId;
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


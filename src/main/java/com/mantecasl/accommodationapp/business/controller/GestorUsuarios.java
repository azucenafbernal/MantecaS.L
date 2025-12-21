package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GestorUsuarios {

    private UsuarioDAO usuarioDAO;

    public GestorUsuarios(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping("/usuarios")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "greeting"; // registro
    }

    @PostMapping("/usuarios")
    public String registrarUsuario(@ModelAttribute Usuario usuario, Model model) {
        if (usuarioDAO.findByEmail(usuario.getEmail()) != null) {
            model.addAttribute("error", "El correo ya está registrado");
            return "greeting";
        }
        Usuario nuevo = usuarioDAO.save(usuario);
        model.addAttribute("usuario", nuevo);

        // después de registrarse, enviamos a login
        model.addAttribute("mensaje", "Registro completado correctamente. Inicia sesión.");
        return "login";
    }
}

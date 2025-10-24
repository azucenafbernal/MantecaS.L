package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class GestorUsuarios {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @GetMapping("/usuarios")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "greeting";  // tu greeting.html
    }

    @PostMapping("/usuarios")
    public String registrarUsuario(@ModelAttribute Usuario usuario, Model model) {
        if (usuarioDAO.findByEmail(usuario.getEmail()) != null) {
            model.addAttribute("error", "El correo ya está registrado");
            return "greeting";
        }
        Usuario nuevo = usuarioDAO.save(usuario);
        model.addAttribute("usuario", nuevo);
        return "result"; // tu result.html
    }
    
}
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
public class LoginController {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "login"; // login.html
    }

    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute Usuario usuario, Model model) {
        Usuario encontrado = usuarioDAO.findByEmail(usuario.getEmail());
        if (encontrado == null || !encontrado.getContrasena().equals(usuario.getContrasena())) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            return "login";
        }
        // Aquí podrías agregar sesión, etc. Por ahora, vamos a un result.html
        model.addAttribute("usuario", encontrado);
        return "result"; // result.html
    }
}

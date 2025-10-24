package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {

    @GetMapping({ "/", "/index" })
    public String mostrarIndex(Model model, HttpSession session) {
        // Obtener usuario de la sesión si existe
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            model.addAttribute("usuario", usuario);
        }
        return "index";
    }
}

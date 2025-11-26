package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @GetMapping("/login")
    public String mostrarLogin(Model model, HttpSession session, @RequestParam(required = false) String from) {
        if (session.getAttribute("usuario") != null) {
            return "redirect:/resultLogin";
        }
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("from", from);
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @ModelAttribute Usuario usuario,
            Model model,
            HttpSession session,
            @RequestParam(required = false) String from) {

        Usuario encontrado = usuarioDAO.findByEmail(usuario.getEmail());

        if (encontrado == null || !encontrado.getContrasena().equals(usuario.getContrasena())) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            model.addAttribute("from", from);
            return "login";
        }

        session.setAttribute("usuario", encontrado);

        if (from != null && !from.isEmpty()) {
            return "redirect:" + from;
        }

        return "redirect:/";
    }


    @GetMapping("/resultLogin")
    public String mostrarResultLogin(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "resultLogin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}

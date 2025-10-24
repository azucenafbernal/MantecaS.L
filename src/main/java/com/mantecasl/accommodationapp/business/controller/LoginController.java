package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @GetMapping("/login")
    public String mostrarLogin(Model model, HttpSession session) {
        //Si ya está logueado, redirigir a resultLogin
        if (session.getAttribute("usuario") != null) {
            return "redirect:/resultLogin";
        }
        model.addAttribute("usuario", new Usuario());
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute Usuario usuario, Model model, HttpSession session) {
        Usuario encontrado = usuarioDAO.findByEmail(usuario.getEmail());
        if (encontrado == null || !encontrado.getContrasena().equals(usuario.getContrasena())) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            return "login";
        }
        //Guardar usuario en sesión
        session.setAttribute("usuario", encontrado);
        return "redirect:/resultLogin";
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

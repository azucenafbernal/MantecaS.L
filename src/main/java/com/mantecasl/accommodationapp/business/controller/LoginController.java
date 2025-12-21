package com.mantecasl.accommodationapp.business.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

import jakarta.servlet.http.HttpSession;
import java.util.Set;

@Controller
public class LoginController {

    private static final String ATTR_USUARIO = "usuario";
    private static final String ATTR_FROM = "from";
    private static final String ATTR_ERROR = "error";
    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_RESULT_LOGIN = "resultLogin";
    private static final String REDIRECT_HOME = "redirect:/";
    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String SESSION_USUARIO_ID = "usuarioId";

    // Whitelist de URLs de redirección permitidas
    private static final Set<String> ALLOWED_REDIRECTS = Set.of(
        "/",
        "/reservas",
        "/propiedades",
        "/favoritos",
        "/notificaciones",
        "/configuracion",
        "/mis-propiedades"
    );

    private final UsuarioDAO usuarioDAO;

    public LoginController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model, HttpSession session, @RequestParam(required = false) String from) {
        if (session.getAttribute(SESSION_USUARIO_ID) != null) {
            return "redirect:/resultLogin";
        }
        model.addAttribute(ATTR_USUARIO, new Usuario());
        model.addAttribute(ATTR_FROM, from);
        return VIEW_LOGIN;
    }

    @PostMapping("/login")
    public String procesarLogin(
            @ModelAttribute Usuario usuario,
            Model model,
            HttpSession session,
            @RequestParam(required = false) String from) {

        Usuario encontrado = usuarioDAO.findByEmail(usuario.getEmail());

        if (encontrado == null || !encontrado.getContrasena().equals(usuario.getContrasena())) {
            model.addAttribute(ATTR_ERROR, "Correo o contraseña incorrectos");
            model.addAttribute(ATTR_FROM, from);
            return VIEW_LOGIN;
        }

        // guardar usuario completo en sesion
        session.setAttribute(ATTR_USUARIO, encontrado);
        session.setAttribute(SESSION_USUARIO_ID, encontrado.getId());

        // Redireccionar solo a URLs en whitelist
        String destino = obtenerRedirectSeguro(from);
        return REDIRECT_PREFIX + destino;
    }

    /**
     * Obtiene un destino de redirección validado contra whitelist.
     * Solo permite redirecciones a URLs predefinidas y seguras.
     */
    private String obtenerRedirectSeguro(String from) {
        if (from != null && ALLOWED_REDIRECTS.contains(from)) {
            return from;
        }
        return "/";
    }

    @GetMapping("/resultLogin")
    public String mostrarResultLogin(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario == null) {
            return REDIRECT_PREFIX + "/login";
        }
        model.addAttribute(ATTR_USUARIO, usuario);
        return VIEW_RESULT_LOGIN;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return REDIRECT_HOME;
    }
}

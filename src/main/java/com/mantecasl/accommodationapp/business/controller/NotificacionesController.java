package com.mantecasl.accommodationapp.business.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mantecasl.accommodationapp.business.entity.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionesController { 

    private static final String ATTR_USUARIO = "usuario";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_NOTIFICACIONES = "redirect:/notificaciones";

    private final GestorNotificaciones gestorNotificaciones;
    private final GestorReservas gestorReservas;

    public NotificacionesController(GestorNotificaciones gestorNotificaciones,
                                   GestorReservas gestorReservas) {
        this.gestorNotificaciones = gestorNotificaciones;
        this.gestorReservas = gestorReservas;
    }

    @GetMapping
    public String listarNotificaciones(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }
        
        model.addAttribute("notificaciones", 
            gestorNotificaciones.obtenerNotificacionesUsuario(usuario));
        model.addAttribute("noLeidas", 
            gestorNotificaciones.contarNotificacionesNoLeidas(usuario));
        
        return "notificacionesUsuario";
    }

    @GetMapping("/propietario")
    public String verNotificacionesPropietario(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);

        if (usuario == null) {
            return REDIRECT_LOGIN;
        }

        Long propietarioId = usuario.getId();

        model.addAttribute("solicitudesPendientes",
                gestorReservas.obtenerSolicitudesPendientesPropietario(propietarioId));

        model.addAttribute("solicitudesAprobadas",
                gestorReservas.obtenerSolicitudesAprobadasPropietario(propietarioId));

        model.addAttribute("solicitudesRechazadas",
                gestorReservas.obtenerSolicitudesRechazadasPropietario(propietarioId));

        model.addAttribute("reservasRecientes",
                gestorReservas.obtenerHistorialReservasPropietario(propietarioId));

        return "notificaciones";
    }

    
    @PostMapping("/{id}/leer")
    public String marcarComoLeida(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario != null) {
            gestorNotificaciones.marcarComoLeida(id);
        }
        return REDIRECT_NOTIFICACIONES;
    }
    
    @PostMapping("/leer-todas")
    public String marcarTodasComoLeidas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario != null) {
            gestorNotificaciones.marcarTodasComoLeidas(usuario);
        }
        return REDIRECT_NOTIFICACIONES;
    }
    
    @PostMapping("/{id}/eliminar")
    public String eliminarNotificacion(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario != null) {
            gestorNotificaciones.eliminarNotificacion(id);
        }
        return REDIRECT_NOTIFICACIONES;
    }
    
    @GetMapping("/contar-no-leidas")
    @ResponseBody
    public long contarNoLeidas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario != null) {
            return gestorNotificaciones.contarNotificacionesNoLeidas(usuario);
        }
        return 0;
    }
}
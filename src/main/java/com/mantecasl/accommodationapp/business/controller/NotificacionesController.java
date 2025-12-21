package com.mantecasl.accommodationapp.business.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mantecasl.accommodationapp.business.entity.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionesController { 
    
    // Constantes para atributos del modelo
    private static final String ATTR_USUARIO = "usuario";
    private static final String ATTR_NOTIFICACIONES = "notificaciones";
    private static final String ATTR_NO_LEIDAS = "noLeidas";
    private static final String ATTR_SOLICITUDES_PENDIENTES = "solicitudesPendientes";
    private static final String ATTR_SOLICITUDES_APROBADAS = "solicitudesAprobadas";
    private static final String ATTR_SOLICITUDES_RECHAZADAS = "solicitudesRechazadas";
    private static final String ATTR_RESERVAS_RECIENTES = "reservasRecientes";
    
    // Constantes para vistas
    private static final String VIEW_NOTIFICACIONES_USUARIO = "notificacionesUsuario";
    private static final String VIEW_NOTIFICACIONES = "notificaciones";
    
    // Constantes para redirects
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_NOTIFICACIONES = "redirect:/notificaciones";
    
    private GestorNotificaciones gestorNotificaciones;
    private GestorReservas gestorReservas;

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
        
        model.addAttribute(ATTR_NOTIFICACIONES, 
            gestorNotificaciones.obtenerNotificacionesUsuario(usuario));
        model.addAttribute(ATTR_NO_LEIDAS, 
            gestorNotificaciones.contarNotificacionesNoLeidas(usuario));
        
        return VIEW_NOTIFICACIONES_USUARIO;
    }

    @GetMapping("/propietario/notificaciones")
    public String verNotificaciones(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }

        Long usuarioId = usuario.getId();

        model.addAttribute(ATTR_SOLICITUDES_PENDIENTES,
                gestorReservas.obtenerSolicitudesPendientesPropietario(usuarioId));

        model.addAttribute(ATTR_SOLICITUDES_APROBADAS,
                gestorReservas.obtenerSolicitudesAprobadasPropietario(usuarioId));

        model.addAttribute(ATTR_SOLICITUDES_RECHAZADAS,
                gestorReservas.obtenerSolicitudesRechazadasPropietario(usuarioId));

        model.addAttribute(ATTR_RESERVAS_RECIENTES,
                gestorReservas.obtenerHistorialReservasPropietario(usuarioId));

        return VIEW_NOTIFICACIONES;
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
    public long contarNoLeidas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario != null) {
            return gestorNotificaciones.contarNotificacionesNoLeidas(usuario);
        }
        return 0;
    }
}
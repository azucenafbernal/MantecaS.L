package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionesController { 
    
    @Autowired
    private GestorNotificaciones gestorNotificaciones;

    @Autowired
    private GestorReservas gestorReservas;

    
    @GetMapping
    public String listarNotificaciones(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("notificaciones", 
            gestorNotificaciones.obtenerNotificacionesUsuario(usuario));
        model.addAttribute("noLeidas", 
            gestorNotificaciones.contarNotificacionesNoLeidas(usuario));
        
        return "notificacionesUsuario";
    }

    @GetMapping("/propietario")
    public String verNotificacionesPropietario(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
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
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            gestorNotificaciones.marcarComoLeida(id);
        }
        return "redirect:/notificaciones";
    }
    
    @PostMapping("/leer-todas")
    public String marcarTodasComoLeidas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            gestorNotificaciones.marcarTodasComoLeidas(usuario);
        }
        return "redirect:/notificaciones";
    }
    
    @PostMapping("/{id}/eliminar")
    public String eliminarNotificacion(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            gestorNotificaciones.eliminarNotificacion(id);
        }
        return "redirect:/notificaciones";
    }
    
    @GetMapping("/contar-no-leidas")
    @ResponseBody
    public long contarNoLeidas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            return gestorNotificaciones.contarNotificacionesNoLeidas(usuario);
        }
        return 0;
    }
}
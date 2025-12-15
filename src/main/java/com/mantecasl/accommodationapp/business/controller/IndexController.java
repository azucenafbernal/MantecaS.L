package com.mantecasl.accommodationapp.business.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.PropietarioDAO;
import com.mantecasl.accommodationapp.business.persistance.SolicitudReservaDAO;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {

    private SolicitudReservaDAO solicitudReservaDAO;
    private PropietarioDAO propietarioDAO;
    private InmuebleDAO inmuebleDAO;
    private GestorNotificaciones gestorNotificaciones; // CAMBIAR

    public IndexController(SolicitudReservaDAO solicitudReservaDAO,
                           PropietarioDAO propietarioDAO,
                           InmuebleDAO inmuebleDAO,
                           GestorNotificaciones gestorNotificaciones) {
        this.solicitudReservaDAO = solicitudReservaDAO;
        this.propietarioDAO = propietarioDAO;
        this.inmuebleDAO = inmuebleDAO;
        this.gestorNotificaciones = gestorNotificaciones;
    }

    @GetMapping({ "/", "/index" })
    public String mostrarIndex(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario != null) {
            model.addAttribute("usuario", usuario);
            
            // Verificar si es propietario
            var propietario = propietarioDAO.findByUsuarioId(usuario.getId());
            if (propietario != null) {
                var inmuebleIds = inmuebleDAO.findAll().stream()
                    .filter(inmueble ->
                        inmueble.getPropietario() != null &&
                        inmueble.getPropietario().getId().equals(propietario.getId()))
                    .map(Inmueble::getId)
                    .toList();
                
                long numeroPendientes = solicitudReservaDAO.findAll().stream()
                    .filter(solicitud -> 
                        inmuebleIds.contains(solicitud.getInmueble().getId()) &&
                        "PENDIENTE".equals(solicitud.getEstado()))
                    .count();
                
                model.addAttribute("numeroNotificacionesPendientes", numeroPendientes);
                model.addAttribute("esPropietario", true);
            } else {
                model.addAttribute("esPropietario", false);
                model.addAttribute("numeroNotificacionesPendientes", 0);
            }
            
            // CONTAR NOTIFICACIONES NO LEÍDAS
            long notificacionesNoLeidas = gestorNotificaciones.contarNotificacionesNoLeidas(usuario);
            model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);
            
        }
        
        return "index";
    }
}
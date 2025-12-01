package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    @Autowired
    private SolicitudReservaDAO solicitudReservaDAO;
    
    @Autowired
    private PropietarioDAO propietarioDAO;
    
    @Autowired
    private InmuebleDAO inmuebleDAO;

    @GetMapping({ "/", "/index" })
    public String mostrarIndex(Model model, HttpSession session) {
        // Obtener usuario de la sesión si existe
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario != null) {
            model.addAttribute("usuario", usuario);
            
            // Verificar si es propietario
            var propietario = propietarioDAO.findByUsuarioId(usuario.getId());
            if (propietario != null) {
                // Obtener IDs de inmuebles del propietario
                var inmuebleIds = inmuebleDAO.findAll().stream()
                    .filter(inmueble -> 
                        inmueble.getPropietario() != null && 
                        inmueble.getPropietario().getId().equals(propietario.getId()))
                    .map(inmueble -> inmueble.getId())
                    .collect(Collectors.toList());
                
                // Contar solicitudes pendientes manualmente
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
        }
        
        return "index";
    }
}
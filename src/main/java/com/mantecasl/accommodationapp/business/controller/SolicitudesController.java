package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/propietario")
public class SolicitudesController {

    @Autowired
    private SolicitudReservaDAO solicitudReservaDAO;

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private DisponibilidadDAO disponibilidadDAO;

    @Autowired
    private GestorDisponibilidad gestorDisponibilidad;

    @Autowired
    private PropietarioDAO propietarioDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @GetMapping("/reserva/{id}")
    public String verSolicitud(@PathVariable Long id,
                            HttpSession session,
                            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
        if (propietario == null) return "redirect:/";

        SolicitudReserva solicitud = solicitudReservaDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!solicitud.getInmueble().getPropietario().getId()
                .equals(propietario.getId())) {
            return "redirect:/";
        }

        model.addAttribute("reserva", solicitud);
        return "peticion";   
    }

    @GetMapping("/notificaciones")
    public String verNotificaciones(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            return "redirect:/login";
        }
        
        //Verificar que es propietario
        Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
        if (propietario == null) {
            // Si no es propietario, redirigir
            return "redirect:/";
        }
        
        //Obtener inmuebles del propietario
        List<Inmueble> inmuebles = inmuebleDAO.findAll().stream()
            .filter(inmueble -> 
                inmueble.getPropietario() != null && 
                inmueble.getPropietario().getId().equals(propietario.getId()))
            .collect(Collectors.toList());
        
        //Obtener IDs de inmuebles
        List<Long> inmuebleIds = inmuebles.stream()
            .map(Inmueble::getId)
            .collect(Collectors.toList());
        
        List<SolicitudReserva> todasSolicitudes = solicitudReservaDAO.findAll();
        
        List<SolicitudReserva> solicitudesPendientes = todasSolicitudes.stream()
            .filter(solicitud -> 
                inmuebleIds.contains(solicitud.getInmueble().getId()) &&
                "PENDIENTE".equals(solicitud.getEstado()))
            .collect(Collectors.toList());
        
        List<SolicitudReserva> solicitudesRecientes = todasSolicitudes.stream()
            .filter(solicitud -> 
                inmuebleIds.contains(solicitud.getInmueble().getId()))
            .limit(10)
            .collect(Collectors.toList());
        
        //Contar notificaciones pendientes
        long numeroPendientes = solicitudesPendientes.size();
        
        model.addAttribute("solicitudesPendientes", solicitudesPendientes);
        model.addAttribute("solicitudesRecientes", solicitudesRecientes);
        model.addAttribute("propietario", propietario);
        model.addAttribute("numeroPendientes", numeroPendientes);
        
        return "notificaciones";
    }

    //Aprobar solicitud
    @PostMapping("/reserva/{id}/aprobar")
    public String aprobarSolicitud(@PathVariable Long id,
                                @RequestParam(required = false) String mensaje,
                                HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
        if (propietario == null) return "redirect:/";

        SolicitudReserva solicitud = solicitudReservaDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!solicitud.getInmueble().getPropietario().getId()
                .equals(propietario.getId())) {
            return "redirect:/";
        }

        //Verificar que aún esté disponible
        if (!gestorDisponibilidad.verificarDisponibilidad(
                solicitud.getInmueble().getId(), 
                solicitud.getFechaInicio(), 
                solicitud.getFechaFin())) {
            // Si ya no está disponible, rechazar automáticamente
            solicitud.rechazar("Fechas no disponibles");
            solicitudReservaDAO.save(solicitud);
            return "redirect:/propietario/notificaciones?error=Fechas+no+disponibles";
        }

        //Aprobar la solicitud
        solicitud.aprobar(mensaje != null ? mensaje : "Reserva aprobada.");
        
        //Crear reserva desde la solicitud (los datos de pago ya están en el inquilino)
        Reserva reserva = solicitud.crearReserva();
        reservaDAO.save(reserva);
        
        //Guardar la solicitud con referencia a la reserva
        solicitudReservaDAO.save(solicitud);

        //Crear Disponibilidad para bloquear fechas
        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setInmueble(solicitud.getInmueble());
        disponibilidad.setFechaInicio(solicitud.getFechaInicio());
        disponibilidad.setFechaFin(solicitud.getFechaFin());
        disponibilidad.setDirecta(false); 
        disponibilidad.setDisponible(false); 
        disponibilidad.setPrecio(solicitud.getPrecioTotal());
        disponibilidadDAO.save(disponibilidad);

        return "redirect:/propietario/notificaciones?exito=Solicitud+aprobada";
    }

    //Rechazar Solicitud
    @PostMapping("/reserva/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable Long id,
                                    @RequestParam String motivo,
                                    HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
        if (propietario == null) return "redirect:/";

        SolicitudReserva solicitud = solicitudReservaDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!solicitud.getInmueble().getPropietario().getId()
                .equals(propietario.getId())) {
            return "redirect:/";
        }

        //Cambiar estado a RECHAZADA
        solicitud.rechazar(motivo);
        solicitudReservaDAO.save(solicitud);

        return "redirect:/propietario/notificaciones?exito=Solicitud+rechazada";
    }
}
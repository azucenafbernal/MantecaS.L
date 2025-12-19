package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.exception.ReservaException;
import com.mantecasl.accommodationapp.business.persistance.*;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/propietario")
public class SolicitudesController {

    private static final String ATTR_USUARIO = "usuario";
    private static final String ATTR_RESERVA = "reserva";
    private static final String ATTR_SOLICITUDES_PENDIENTES = "solicitudesPendientes";
    private static final String ATTR_SOLICITUDES_RECIENTES = "solicitudesRecientes";
    private static final String ATTR_PROPIETARIO = "propietario";
    private static final String ATTR_NUMERO_PENDIENTES = "numeroPendientes";

    private static final String VIEW_PETICION = "peticion";
    private static final String VIEW_NOTIFICACIONES = "notificaciones";

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_HOME = "redirect:/";
    private static final String REDIRECT_NOTIFICACIONES = "redirect:/propietario/notificaciones";
    private static final String REDIRECT_NOTIFICACIONES_ERROR_FECHAS = REDIRECT_NOTIFICACIONES + "?error=Fechas+no+disponibles";
    private static final String REDIRECT_NOTIFICACIONES_EXITO_APROBADA = REDIRECT_NOTIFICACIONES + "?exito=Solicitud+aprobada";
    private static final String REDIRECT_NOTIFICACIONES_EXITO_RECHAZADA = REDIRECT_NOTIFICACIONES + "?exito=Solicitud+rechazada";

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String MSG_SOLICITUD_NO_ENCONTRADA = "Solicitud no encontrada";
    private static final String MSG_FECHAS_NO_DISPONIBLES = "Fechas no disponibles";
    private static final String MSG_RESERVA_APROBADA = "Reserva aprobada.";

    private final SolicitudReservaDAO solicitudReservaDAO;
    private final ReservaDAO reservaDAO;
    private final DisponibilidadDAO disponibilidadDAO;
    private final GestorDisponibilidad gestorDisponibilidad;
    private final PropietarioDAO propietarioDAO;
    private final GestorNotificaciones notificacion;
    private final InmuebleDAO inmuebleDAO;

    public SolicitudesController(SolicitudReservaDAO solicitudReservaDAO,
                                 ReservaDAO reservaDAO,
                                 DisponibilidadDAO disponibilidadDAO,
                                 GestorDisponibilidad gestorDisponibilidad,
                                 PropietarioDAO propietarioDAO,
                                 GestorNotificaciones notificacion,
                                 InmuebleDAO inmuebleDAO) {
        this.solicitudReservaDAO = solicitudReservaDAO;
        this.reservaDAO = reservaDAO;
        this.disponibilidadDAO = disponibilidadDAO;
        this.gestorDisponibilidad = gestorDisponibilidad;
        this.propietarioDAO = propietarioDAO;
        this.notificacion = notificacion;
        this.inmuebleDAO = inmuebleDAO;
    }

    @GetMapping("/reserva/{id}")
    public String verSolicitud(@PathVariable Long id,
                            HttpSession session,
                            Model model) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario == null) return REDIRECT_LOGIN;

        Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
        if (propietario == null) return REDIRECT_HOME;

        SolicitudReserva solicitud = solicitudReservaDAO.findById(id)
            .orElseThrow(() -> new ReservaException(MSG_SOLICITUD_NO_ENCONTRADA));

        if (!solicitud.getInmueble().getPropietario().getId()
                .equals(propietario.getId())) {
            return REDIRECT_HOME;
        }

        model.addAttribute(ATTR_RESERVA, solicitud);
        return VIEW_PETICION;   
    }

    @GetMapping("/notificaciones")
    public String verNotificaciones(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }
        
        //Verificar que es propietario
        Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
        if (propietario == null) {
            // Si no es propietario, redirigir
            return REDIRECT_HOME;
        }
        
        //Obtener inmuebles del propietario
        List<Inmueble> inmuebles = inmuebleDAO.findAll().stream()
            .filter(inmueble -> 
                inmueble.getPropietario() != null && 
                inmueble.getPropietario().getId().equals(propietario.getId()))
            .toList();
        
        //Obtener IDs de inmuebles
        List<Long> inmuebleIds = inmuebles.stream()
            .map(Inmueble::getId)
            .toList();
        
        List<SolicitudReserva> todasSolicitudes = solicitudReservaDAO.findAll();
        
        List<SolicitudReserva> solicitudesPendientes = todasSolicitudes.stream()
            .filter(solicitud -> 
                inmuebleIds.contains(solicitud.getInmueble().getId()) &&
                ESTADO_PENDIENTE.equals(solicitud.getEstado()))
            .toList();
        
        List<SolicitudReserva> solicitudesRecientes = todasSolicitudes.stream()
            .filter(solicitud -> 
                inmuebleIds.contains(solicitud.getInmueble().getId()))
            .limit(10)
            .toList();
        
        //Contar notificaciones pendientes
        long numeroPendientes = solicitudesPendientes.size();
        
        model.addAttribute(ATTR_SOLICITUDES_PENDIENTES, solicitudesPendientes);
        model.addAttribute(ATTR_SOLICITUDES_RECIENTES, solicitudesRecientes);
        model.addAttribute(ATTR_PROPIETARIO, propietario);
        model.addAttribute(ATTR_NUMERO_PENDIENTES, numeroPendientes);
        
        return VIEW_NOTIFICACIONES;
    }

    //Aprobar solicitud
    @PostMapping("/reserva/{id}/aprobar")
    public String aprobarSolicitud(@PathVariable Long id,
                                @RequestParam(required = false) String mensaje,
                                HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario == null) return REDIRECT_LOGIN;

        Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
        if (propietario == null) return REDIRECT_HOME;

        SolicitudReserva solicitud = solicitudReservaDAO.findById(id)
            .orElseThrow(() -> new ReservaException(MSG_SOLICITUD_NO_ENCONTRADA));

        if (!solicitud.getInmueble().getPropietario().getId()
                .equals(propietario.getId())) {
            return REDIRECT_HOME;
        }

        //Verificar que aún esté disponible
        if (!gestorDisponibilidad.verificarDisponibilidad(
                solicitud.getInmueble().getId(), 
                solicitud.getFechaInicio(), 
                solicitud.getFechaFin())) {
            // Si ya no está disponible, rechazar automáticamente
            solicitud.rechazar(MSG_FECHAS_NO_DISPONIBLES);
            solicitudReservaDAO.save(solicitud);
            return REDIRECT_NOTIFICACIONES_ERROR_FECHAS;
        }

        //Aprobar la solicitud
        solicitud.aprobar(mensaje != null ? mensaje : MSG_RESERVA_APROBADA);
        
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

        notificacion.crearNotificacionSolicitudAprobada(solicitud);

        return REDIRECT_NOTIFICACIONES_EXITO_APROBADA;
    }

    //Rechazar Solicitud
    @PostMapping("/reserva/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable Long id, @RequestParam String motivo, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario == null) return REDIRECT_LOGIN;

        Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
        if (propietario == null) return REDIRECT_HOME;

        SolicitudReserva solicitud = solicitudReservaDAO.findById(id)
            .orElseThrow(() -> new ReservaException(MSG_SOLICITUD_NO_ENCONTRADA));

        if (!solicitud.getInmueble().getPropietario().getId()
                .equals(propietario.getId())) {
            return REDIRECT_HOME;
        }

        solicitud.rechazar(motivo);
        solicitudReservaDAO.save(solicitud);

        notificacion.crearNotificacionSolicitudRechazada(solicitud, motivo);
        return REDIRECT_NOTIFICACIONES_EXITO_RECHAZADA;
    }
}
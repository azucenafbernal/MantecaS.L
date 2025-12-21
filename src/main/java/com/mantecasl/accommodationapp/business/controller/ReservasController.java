package com.mantecasl.accommodationapp.business.controller;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mantecasl.accommodationapp.business.entity.Disponibilidad;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Inquilino;
import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.entity.SolicitudReserva;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.exception.ReservaException;
import com.mantecasl.accommodationapp.business.persistance.DisponibilidadDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.InquilinoDAO;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;
import com.mantecasl.accommodationapp.business.persistance.SolicitudReservaDAO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reservas")
public class ReservasController {

    private static final String ATTR_USUARIO = "usuario";
    private static final String ATTR_INMUEBLE = "inmueble";
    private static final String ATTR_FECHA_INICIO = "fechaInicio";
    private static final String ATTR_FECHA_FIN = "fechaFin";
    private static final String ATTR_ES_RESERVA_DIRECTA = "esReservaDirecta";
    private static final String ATTR_RESERVA = "reserva";
    private static final String ATTR_DISPONIBILIDAD = "disponibilidad";
    private static final String ATTR_MENSAJE = "mensaje";
    private static final String ATTR_ES_DIRECTA = "esDirecta";
    private static final String ATTR_SOLICITUD = "solicitud";
    private static final String ATTR_ERROR = "error";

    private static final String VIEW_RESERVA_INMUEBLE = "reserva-inmueble";
    private static final String VIEW_CONFIRMACION_RESERVA = "confirmacion-reserva";

    private static final String REDIRECT_LOGIN_FROM_RESERVA = "redirect:/login?from=/reservas/nueva/";
    private static final String REDIRECT_HOME = "redirect:/";

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String MSG_NO_DISPONIBLE = "El inmueble no está disponible en las fechas seleccionadas.";
    private static final String MSG_SOLICITUD_EXISTENTE = "Ya existe una solicitud pendiente para estas fechas. Espera la respuesta del propietario.";

    private final GestorDisponibilidad gestorDisponibilidad;
    private final InmuebleDAO inmuebleDAO;
    private final ReservaDAO reservaDAO;
    private final InquilinoDAO inquilinoDAO;
    private final SolicitudReservaDAO solicitudReservaDAO;
    private final DisponibilidadDAO disponibilidadDAO;
    private final GestorNotificaciones notificacion;

    public ReservasController(GestorDisponibilidad gestorDisponibilidad,
                              InmuebleDAO inmuebleDAO,
                              ReservaDAO reservaDAO,
                              InquilinoDAO inquilinoDAO,
                              SolicitudReservaDAO solicitudReservaDAO,
                              DisponibilidadDAO disponibilidadDAO,
                              GestorNotificaciones notificacion) {
        this.gestorDisponibilidad = gestorDisponibilidad;
        this.inmuebleDAO = inmuebleDAO;
        this.reservaDAO = reservaDAO;
        this.inquilinoDAO = inquilinoDAO;
        this.solicitudReservaDAO = solicitudReservaDAO;
        this.disponibilidadDAO = disponibilidadDAO;
        this.notificacion = notificacion;
    }

    @GetMapping("/nueva/{inmuebleId}")
    public String mostrarFormularioReserva(
            @PathVariable Long inmuebleId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);

        if (usuario == null) {
            return REDIRECT_LOGIN_FROM_RESERVA + inmuebleId;
        }

        Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
        if (inmuebleOpt.isEmpty()) {
            return REDIRECT_HOME;
        }

        Inmueble inmueble = inmuebleOpt.get();

        // Añadir al modelo si el inmueble tiene reserva directa o por confirmación
        model.addAttribute(ATTR_ES_RESERVA_DIRECTA, inmueble.isReservaDirecta());
        model.addAttribute(ATTR_USUARIO, usuario);
        model.addAttribute(ATTR_INMUEBLE, inmueble);
        model.addAttribute(ATTR_FECHA_INICIO, fechaInicio);
        model.addAttribute(ATTR_FECHA_FIN, fechaFin);

        return VIEW_RESERVA_INMUEBLE;
    }

    @PostMapping("/confirmar")
    public String confirmarReserva(
            @RequestParam Long inmuebleId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam String telefono,
            @RequestParam String documentoIdentidad,
            @RequestParam String metodoPago,
            @RequestParam(required = false) String numeroTarjeta,
            @RequestParam(required = false) String fechaCaducidad,
            @RequestParam(required = false) String cvv,
            @RequestParam(required = false) String paypalEmail,
            @RequestParam(required = false) String observaciones,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);

        if (usuario == null) {
            return REDIRECT_LOGIN_FROM_RESERVA + inmuebleId;
        }

        try {
            // Validar fechas
            Date[] fechas = gestorDisponibilidad.validarFechas(fechaInicio, fechaFin);
            Date inicio = fechas[0];
            Date fin = fechas[1];

            // Obtener el inmueble con su configuración de reserva
            Inmueble inmueble = inmuebleDAO.findById(inmuebleId)
                    .orElseThrow(() -> new RuntimeException("Inmueble no encontrado"));

            // OBTENER EL TIPO DE RESERVA DEL INMUEBLE, NO DEL FORMULARIO
            boolean esReservaDirecta = inmueble.isReservaDirecta();

            // Buscar o crear inquilino
            Inquilino inquilino = inquilinoDAO.findByUsuario(usuario).orElseGet(() -> {
                Inquilino nuevo = new Inquilino(usuario, telefono, documentoIdentidad, metodoPago);
                nuevo.setInmueble(inmueble);
                return inquilinoDAO.save(nuevo);
            });

            // Actualizar inquilino existente con datos de pago
            inquilino.setTelefono(telefono);
            inquilino.setDocumentoIdentidad(documentoIdentidad);
            inquilino.setInmueble(inmueble);
            inquilino.actualizarDatosPago(metodoPago, numeroTarjeta, fechaCaducidad, cvv, paypalEmail);
            inquilinoDAO.save(inquilino);

            // Calcular precio total
            long noches = (fin.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24);
            double precioTotal = noches * inmueble.getPrecioNoche();

            // USAR esReservaDirecta (del inmueble) en lugar de directa (del formulario)
            if (esReservaDirecta) {
                if (!gestorDisponibilidad.verificarDisponibilidad(inmuebleId, inicio, fin)) {
                        throw new ReservaException(MSG_NO_DISPONIBLE);
                }

                // Crear disponibilidad bloqueada
                Disponibilidad disponibilidad = new Disponibilidad();
                disponibilidad.setInmueble(inmueble);
                disponibilidad.setFechaInicio(inicio);
                disponibilidad.setFechaFin(fin);
                disponibilidad.setDirecta(true); // Siempre true para reserva directa
                disponibilidad.setDisponible(false);
                disponibilidad.setPrecio(precioTotal);
                disponibilidadDAO.save(disponibilidad);

                // Crear reserva confirmada inmediatamente
                Reserva reserva = new Reserva(inmueble, inquilino, inicio, fin, precioTotal);
                reserva.confirmar();
                reservaDAO.save(reserva);

                model.addAttribute(ATTR_RESERVA, reserva);
                model.addAttribute(ATTR_DISPONIBILIDAD, disponibilidad);
                model.addAttribute(ATTR_MENSAJE, "¡Reserva confirmada y pagada exitosamente!");
                model.addAttribute(ATTR_ES_DIRECTA, true);

                notificacion.crearNotificacionReservaDirecta(reserva);

            } else {
                // RESERVA POR CONFIRMACIÓN
                List<SolicitudReserva> solicitudesExistentes = solicitudReservaDAO.findAll().stream()
                    .filter(s -> s.getInmueble().getId().equals(inmuebleId) &&
                                s.getEstado().equals(ESTADO_PENDIENTE) &&
                                seSolapan(s.getFechaInicio(), s.getFechaFin(), inicio, fin))
                    .toList();
                
                if (!solicitudesExistentes.isEmpty()) {
                    throw new ReservaException(MSG_SOLICITUD_EXISTENTE);
                }

                // Verificar disponibilidad para solicitud
                if (!gestorDisponibilidad.verificarDisponibilidadParaSolicitud(inmuebleId, inicio, fin)) {
                    throw new ReservaException(MSG_NO_DISPONIBLE);
                }

                // Crear solicitud pendiente de aprobación
                SolicitudReserva solicitud = new SolicitudReserva(inquilino, inmueble, inicio, fin, precioTotal);
                solicitud.setObservacionesInquilino(observaciones);
                solicitudReservaDAO.save(solicitud);

                model.addAttribute(ATTR_SOLICITUD, solicitud);
                model.addAttribute(ATTR_MENSAJE, "¡Solicitud de reserva enviada! El propietario la revisará pronto. Solo pagarás cuando sea aprobada.");
                model.addAttribute(ATTR_ES_DIRECTA, false);
            }

            model.addAttribute(ATTR_INMUEBLE, inmueble);

            return VIEW_CONFIRMACION_RESERVA;

        } catch (Exception e) {
            model.addAttribute(ATTR_ERROR, e.getMessage());
            Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
            if (inmuebleOpt.isPresent()) {
                Inmueble inmueble = inmuebleOpt.get();
                model.addAttribute(ATTR_INMUEBLE, inmueble);
                model.addAttribute(ATTR_ES_RESERVA_DIRECTA, inmueble.isReservaDirecta());
            }
            model.addAttribute(ATTR_USUARIO, usuario);
            model.addAttribute(ATTR_FECHA_INICIO, fechaInicio);
            model.addAttribute(ATTR_FECHA_FIN, fechaFin);

            return VIEW_RESERVA_INMUEBLE;
        }
    }

    private boolean seSolapan(Date inicio1, Date fin1, Date inicio2, Date fin2) {
        return (inicio1.before(fin2) && inicio2.before(fin1));
    }
}
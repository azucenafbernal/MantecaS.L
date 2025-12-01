package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired
    private GestorDisponibilidad gestorDisponibilidad;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private InquilinoDAO inquilinoDAO;

    @Autowired
    private SolicitudReservaDAO solicitudReservaDAO;

    @Autowired
    private DisponibilidadDAO disponibilidadDAO;

    @GetMapping("/nueva/{inmuebleId}")
    public String mostrarFormularioReserva(
            @PathVariable Long inmuebleId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login?from=/reservas/nueva/" + inmuebleId;
        }

        Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
        if (inmuebleOpt.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("inmueble", inmuebleOpt.get());
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "reserva-inmueble";
    }

    @PostMapping("/confirmar")
    public String confirmarReserva(
            @RequestParam Long inmuebleId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam boolean directa,
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

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login?from=/reservas/nueva/" + inmuebleId;
        }

        try {
            // Validar fechas
            Date[] fechas = gestorDisponibilidad.validarFechas(fechaInicio, fechaFin);
            Date inicio = fechas[0];
            Date fin = fechas[1];

            // Obtener el inmueble
            Inmueble inmueble = inmuebleDAO.findById(inmuebleId)
                    .orElseThrow(() -> new RuntimeException("Inmueble no encontrado"));

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

            if (directa) {
                
                if (!gestorDisponibilidad.verificarDisponibilidad(inmuebleId, inicio, fin)) {
                    throw new RuntimeException("El inmueble no está disponible en las fechas seleccionadas.");
                }

                Disponibilidad disponibilidad = new Disponibilidad();
                disponibilidad.setInmueble(inmueble);
                disponibilidad.setFechaInicio(inicio);
                disponibilidad.setFechaFin(fin);
                disponibilidad.setDirecta(true);
                disponibilidad.setDisponible(false);
                disponibilidad.setPrecio(precioTotal);
                disponibilidadDAO.save(disponibilidad);

                Reserva reserva = new Reserva(inmueble, inquilino, inicio, fin, precioTotal);
                reserva.confirmar();
                reservaDAO.save(reserva);

                model.addAttribute("reserva", reserva);
                model.addAttribute("disponibilidad", disponibilidad);
                model.addAttribute("mensaje", "¡Reserva confirmada exitosamente!");
                model.addAttribute("esDirecta", true);

            } else {
                
                List<SolicitudReserva> solicitudesExistentes = solicitudReservaDAO.findAll().stream()
                    .filter(s -> s.getInmueble().getId().equals(inmuebleId) &&
                                s.getEstado().equals("PENDIENTE") &&
                                seSolapan(s.getFechaInicio(), s.getFechaFin(), inicio, fin))
                    .collect(Collectors.toList());
                
                if (!solicitudesExistentes.isEmpty()) {
                    throw new RuntimeException("Ya existe una solicitud pendiente para estas fechas. Espera la respuesta del propietario.");
                }

                SolicitudReserva solicitud = new SolicitudReserva(inquilino, inmueble, inicio, fin, precioTotal);
                solicitud.setObservacionesInquilino(observaciones);
                solicitudReservaDAO.save(solicitud);

                model.addAttribute("solicitud", solicitud);
                model.addAttribute("mensaje", "¡Solicitud de reserva enviada! El propietario la revisará pronto.");
                model.addAttribute("esDirecta", false);
            }

            model.addAttribute("inmueble", inmueble);

            return "confirmacion-reserva";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
            inmuebleOpt.ifPresent(i -> model.addAttribute("inmueble", i));
            model.addAttribute("usuario", usuario);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);

            return "reserva-inmueble";
        }
    }

    private boolean seSolapan(Date inicio1, Date fin1, Date inicio2, Date fin2) {
        return (inicio1.before(fin2) && inicio2.before(fin1));
    }
}
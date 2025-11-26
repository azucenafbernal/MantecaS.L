package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Optional;

@Controller
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired
    private GestorDisponibilidad gestorDisponibilidad;

    @Autowired
    private InmuebleDAO inmuebleDAO;

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
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login?from=/reservas/nueva/" + inmuebleId;
        }

        try {
            Date[] fechas = gestorDisponibilidad.validarFechas(fechaInicio, fechaFin);
            Date inicio = fechas[0];
            Date fin = fechas[1];

            Disponibilidad reserva = gestorDisponibilidad.crearReserva(
                    inmuebleId, inicio, fin, directa);

            model.addAttribute("reserva", reserva);
            model.addAttribute("inmueble", reserva.getInmueble());
            model.addAttribute("mensaje", "¡Reserva confirmada con éxito!");

            return "confirmacion-reserva";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
            inmuebleOpt.ifPresent(i -> model.addAttribute("inmueble", i));
            model.addAttribute("usuario", usuario);
            return "reserva-inmueble";
        }
    }
}


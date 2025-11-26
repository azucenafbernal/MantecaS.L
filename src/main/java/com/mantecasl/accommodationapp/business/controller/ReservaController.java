/*package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Disponibilidad;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Optional;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private GestorDisponibilidad gestorDisponibilidad;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    // Mostrar formulario de reserva
    @GetMapping("/nueva/{inmuebleId}")
    public String mostrarFormularioReserva(@PathVariable Long inmuebleId, Model model) {
        Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
        if (!inmuebleOpt.isPresent()) {
            return "redirect:/";
        }

        model.addAttribute("inmueble", inmuebleOpt.get());
        return "reserva-inmueble";
    }

    // Procesar reserva
    @PostMapping("/confirmar")
    public String confirmarReserva(
            @RequestParam Long inmuebleId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam boolean directa,
            Model model) {
        
        try {
            // Validar fechas
            Date[] fechas = gestorDisponibilidad.validarFechas(fechaInicio, fechaFin);
            Date inicio = fechas[0];
            Date fin = fechas[1];

            // Crear la reserva
            Disponibilidad reserva = gestorDisponibilidad.crearReserva(inmuebleId, inicio, fin, directa);

            // Pasar datos a la vista de confirmación
            model.addAttribute("reserva", reserva);
            model.addAttribute("inmueble", reserva.getInmueble());
            model.addAttribute("mensaje", "¡Reserva confirmada con éxito!");

            return "confirmacion-reserva";

        } catch (Exception e) {
            // Si hay error, volver al formulario con mensaje de error
            Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
            if (inmuebleOpt.isPresent()) {
                model.addAttribute("inmueble", inmuebleOpt.get());
            }
            model.addAttribute("error", e.getMessage());
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            return "reserva-inmueble";
        }
    }
}*/
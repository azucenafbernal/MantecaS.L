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
    private GestorReservas gestorReservas;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    // Formulario de reserva
    @GetMapping("/reservas/nueva/{inmuebleId}")
    public String mostrarFormularioReserva(@PathVariable Long inmuebleId, 
                                        @RequestParam(required = false) String fechaInicio,
                                        @RequestParam(required = false) String fechaFin,
                                        HttpSession session,
                                        Model model) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
        if (!inmuebleOpt.isPresent()) {
            return "redirect:/";
        }

        model.addAttribute("inmueble", inmuebleOpt.get());
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("usuario", usuario);
        
        // Pasar la URL actual como "from"
        String currentUrl = "/reservas/nueva/" + inmuebleId;
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            currentUrl += "?fechaInicio=" + fechaInicio;
            if (fechaFin != null && !fechaFin.isEmpty()) {
                currentUrl += "&fechaFin=" + fechaFin;
            }
        }
        model.addAttribute("fromUrl", currentUrl);

        return "reserva-inmueble";
    }

    // Procesar reserva
    @PostMapping("/reservas/confirmar")
    public String confirmarReserva(
            @RequestParam Long inmuebleId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam boolean directa,
            HttpSession session,  
            Model model) {
        
        // OBTENER USUARIO DE LA SESIÓN
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        // Verificar que el usuario está logueado
        if (usuario == null) {
            model.addAttribute("error", "Debes iniciar sesión para realizar una reserva");
            Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
            if (inmuebleOpt.isPresent()) {
                model.addAttribute("inmueble", inmuebleOpt.get());
            }
            return "reserva-inmueble";
        }

        try {
            Date[] fechas = gestorDisponibilidad.validarFechas(fechaInicio, fechaFin);
            Date inicio = fechas[0];
            Date fin = fechas[1];

            // Crear la reserva usando el usuario de la sesión
            Disponibilidad reserva = gestorDisponibilidad.crearReserva(inmuebleId, inicio, fin, directa);

            model.addAttribute("reserva", reserva);
            model.addAttribute("inmueble", reserva.getInmueble());
            model.addAttribute("mensaje", "¡Reserva confirmada con éxito!");

            return "confirmacion-reserva";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
            if (inmuebleOpt.isPresent()) {
                model.addAttribute("inmueble", inmuebleOpt.get());
            }
            model.addAttribute("usuario", usuario); 
            return "reserva-inmueble";
        }
    }
}

package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class BusquedaController {

    @Autowired
    private GestorDisponibilidad gestorDisponibilidad;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    // ---------------------- BUSCAR DESDE INDEX ----------------------
    @GetMapping("/buscar")
    public String buscarInmuebles(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Integer capacidad,
            Model model
    ) {
        Date[] fechas = gestorDisponibilidad.validarFechas(fechaInicio, fechaFin);
        Date entrada = fechas[0];
        Date salida = fechas[1];

        List<Inmueble> disponibles =
                gestorDisponibilidad.buscarInmueblesDisponibles(entrada, salida, ciudad, capacidad);

        model.addAttribute("inmuebles", disponibles);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "lista-propiedades";
    }

    // ---------------------- IR A DETALLE DE INMUEBLE ----------------------
    @GetMapping("/inmueble/{id}")
    public String verInmueble(@PathVariable Long id, Model model) {
        Optional<Inmueble> opt = inmuebleDAO.findById(id);
        if (!opt.isPresent()) return "redirect:/";

        model.addAttribute("inmueble", opt.get());
        return "detalle-inmueble";
    }
}

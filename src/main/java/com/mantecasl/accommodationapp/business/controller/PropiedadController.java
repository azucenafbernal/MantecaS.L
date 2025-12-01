package com.mantecasl.accommodationapp.business.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;

@Controller
public class PropiedadController {

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private GestorDisponibilidad gestorDisponibilidad;

    /**
     * Catálogo con filtros de disponibilidad, ciudad y capacidad
     */
    @GetMapping("/catalogo")
    public String verCatalogo(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Integer capacidad,
            Model model) {
        
        // Obtenemos todos los inmuebles de la base de datos
        List<Inmueble> todasLasPropiedades = inmuebleDAO.findAll();
        
        // FILTRAR: Solo propiedades que tengan propietario (evitar propiedades eliminadas)
        List<Inmueble> propiedadesValidas = todasLasPropiedades.stream()
                .filter(inmueble -> inmueble.getPropietario() != null)
                .toList(); // Manteniendo tu formato .toList()
        
        // Filtrar según los parámetros recibidos
        List<Inmueble> propiedadesFiltradas = new ArrayList<>(propiedadesValidas);
        
        // ✔️ Si hay fechas especificadas, filtrar por disponibilidad
        if (fechaInicio != null && !fechaInicio.isEmpty() && 
            fechaFin != null && !fechaFin.isEmpty()) {
            
            try {
                LocalDate inicio = LocalDate.parse(fechaInicio);
                LocalDate fin = LocalDate.parse(fechaFin);
                Date sqlInicio = Date.valueOf(inicio);
                Date sqlFin = Date.valueOf(fin);

                // Validar fechas
                if (inicio.isBefore(LocalDate.now())) {
                    model.addAttribute("error", "La fecha de inicio no puede ser en el pasado");
                    model.addAttribute("propiedades", new ArrayList<>());
                    return "lista-propiedades";
                }

                if (fin.isBefore(inicio) || fin.isEqual(inicio)) {
                    model.addAttribute("error", "La fecha de salida debe ser posterior a la de entrada");
                    model.addAttribute("propiedades", new ArrayList<>());
                    return "lista-propiedades";
                }

                // Filtrar solo los que estén disponibles en las fechas solicitadas
                propiedadesFiltradas = propiedadesFiltradas.stream()
                    .filter(inmueble -> gestorDisponibilidad.verificarDisponibilidad(inmueble.getId(), sqlInicio, sqlFin))
                    .collect(Collectors.toList());

            } catch (Exception e) {
                model.addAttribute("error", "Formato de fecha inválido: " + e.getMessage());
                model.addAttribute("propiedades", new ArrayList<>());
                return "lista-propiedades";
            }
        }

        // ✔️ Filtrar por ciudad si se especifica
        if (ciudad != null && !ciudad.isEmpty()) {
            propiedadesFiltradas = propiedadesFiltradas.stream()
                .filter(inmueble -> inmueble.getCiudad() != null && 
                        inmueble.getCiudad().toLowerCase().contains(ciudad.toLowerCase()))
                .collect(Collectors.toList());
        }

        // ✔️ Filtrar por capacidad si se especifica
        if (capacidad != null && capacidad > 0) {
            propiedadesFiltradas = propiedadesFiltradas.stream()
                .filter(inmueble -> inmueble.getCapacidad() >= capacidad)
                .collect(Collectors.toList());
        }

        // Pasar los filtros aplicados para mostrarlos en la vista
        model.addAttribute("propiedades", propiedadesFiltradas);
        model.addAttribute("filtroCiudad", ciudad);
        model.addAttribute("filtroCapacidad", capacidad);
        model.addAttribute("filtroFechaInicio", fechaInicio);
        model.addAttribute("filtroFechaFin", fechaFin);

        if (propiedadesFiltradas.isEmpty()) {
            model.addAttribute("mensaje", "No se encontraron propiedades disponibles con los criterios especificados");
        }

        return "lista-propiedades";
    }
}
package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PropiedadController {

    @Autowired
    private InmuebleDAO inmuebleDAO;

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
        List<Inmueble> propiedadesFiltradas = filtrarPropiedades(propiedadesValidas, ciudad, capacidad);
        
        model.addAttribute("propiedades", propiedadesFiltradas);
        
        // Pasar los filtros aplicados para mostrarlos en la vista
        model.addAttribute("filtroCiudad", ciudad);
        model.addAttribute("filtroCapacidad", capacidad);
        model.addAttribute("filtroFechaInicio", fechaInicio);
        model.addAttribute("filtroFechaFin", fechaFin);
        
        return "lista-propiedades";
    }
    
    private List<Inmueble> filtrarPropiedades(List<Inmueble> propiedades, String ciudad, Integer capacidad) {
        // Si no hay filtros, devolver todas las propiedades
        if ((ciudad == null || ciudad.trim().isEmpty()) && capacidad == null) {
            return propiedades;
        }
        
        return propiedades.stream()
                .filter(inmueble -> {
                    boolean coincide = true;
                    
                    // Filtro por ciudad
                    if (ciudad != null && !ciudad.trim().isEmpty()) {
                        coincide = coincide && inmueble.getCiudad().toLowerCase().contains(ciudad.toLowerCase());
                    }
                    
                    // Filtro por capacidad
                    if (capacidad != null) {
                        coincide = coincide && inmueble.getCapacidad() >= capacidad;
                    }
                    
                    return coincide;
                })
                .toList();
    }
}
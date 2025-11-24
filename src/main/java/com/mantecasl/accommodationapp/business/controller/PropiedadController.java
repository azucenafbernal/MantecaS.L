package com.mantecasl.accommodationapp.business.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;

@Controller
public class PropiedadController {

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @GetMapping("/catalogo")
    public String verCatalogo(Model model) {
        // Obtenemos todos los inmuebles de la base de datos
        List<Inmueble> lista = inmuebleDAO.findAll();
        model.addAttribute("propiedades", lista);
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

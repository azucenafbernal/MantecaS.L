package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.util.List;

@Controller
public class BusquedaController {
    
    @Autowired
    private GestorDisponibilidad gestorDisponibilidad;
    
    @GetMapping("/buscar")
    public String buscarAlojamientos(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Integer capacidad,
            Model model) {
        
        try {
            // Convertir y validar fechas usando el GestorDisponibilidad
            Date[] fechasValidadas = gestorDisponibilidad.validarFechas(fechaInicio, fechaFin);
            Date fechaInicioSql = fechasValidadas[0];
            Date fechaFinSql = fechasValidadas[1];
            
            // Buscar inmuebles disponibles
            List<Inmueble> inmuebles = gestorDisponibilidad.buscarInmueblesDisponibles(
                fechaInicioSql, fechaFinSql, ciudad, capacidad);
            
            model.addAttribute("inmuebles", inmuebles);
            model.addAttribute("fechaInicio", fechaInicioSql);
            model.addAttribute("fechaFin", fechaFinSql);
            model.addAttribute("ciudad", ciudad);
            model.addAttribute("capacidad", capacidad);
            model.addAttribute("resultadosCount", inmuebles.size());
            
            return "resultados-busqueda";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }
}


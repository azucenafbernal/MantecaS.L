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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
public class PropiedadController {

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private GestorDisponibilidad gestorDisponibilidad;

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private GestorNotificaciones notificacion;

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

        if (ciudad != null && !ciudad.isEmpty()) {
            propiedadesFiltradas = propiedadesFiltradas.stream()
                .filter(inmueble -> inmueble.getCiudad() != null && 
                        inmueble.getCiudad().toLowerCase().contains(ciudad.toLowerCase()))
                .collect(Collectors.toList());
        }

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

    @PostMapping("/propiedades/{id}/eliminar")
    @Transactional
    public String eliminarPropiedad(@PathVariable Long id, HttpSession session, Model model) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Inmueble inmueble = inmuebleDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));
            
            // Verificar que el usuario es el propietario
            if (!inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
                model.addAttribute("error", "No tienes permiso para eliminar esta propiedad");
                return "redirect:/propiedades/" + id;
            }
            
            // Obtener reservas futuras
            List<Reserva> reservasFuturas = reservaDAO.findReservasFuturasByInmueble(id);
            
            // NOTIFICAR ELIMINACIÓN
            notificacion.notificarEliminacionPropiedad(inmueble, reservasFuturas);
            
            // Reembolsar pagos (implementar tu lógica de reembolso)
            for (Reserva reserva : reservasFuturas) {
                // Lógica de reembolso aquí
                notificacion.crearNotificacionPagoDevuelto(reserva, reserva.getPrecioTotal());
            }
            
            // Eliminar el inmueble
            inmuebleDAO.delete(inmueble);
            
            model.addAttribute("mensaje", "Propiedad eliminada correctamente. Se han notificado a los inquilinos afectados.");
            return "redirect:/propiedades";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar propiedad: " + e.getMessage());
            return "redirect:/propiedades/" + id;
        }
    }
}

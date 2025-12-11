package com.mantecasl.accommodationapp.business.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
public class PropiedadController {

    private static final String ATTR_USUARIO = "usuario";
    private static final String ATTR_ERROR = "error";
    private static final String ATTR_PROPIEDADES = "propiedades";
    private static final String VIEW_LISTA_PROPIEDADES = "lista-propiedades";
    private static final String VIEW_ERROR_CATALOGO = "error-catalogo";
    private static final String VIEW_ERROR_ELIMINAR = "error-eliminar";
    private static final String ATTR_FILTRO_CIUDAD = "filtroCiudad";
    private static final String ATTR_FILTRO_CAPACIDAD = "filtroCapacidad";
    private static final String ATTR_FILTRO_FECHA_INICIO = "filtroFechaInicio";
    private static final String ATTR_FILTRO_FECHA_FIN = "filtroFechaFin";
    private static final String ATTR_MENSAJE = "mensaje";
    private static final String REDIRECT_HOME = "redirect:/propiedades";

    private final InmuebleDAO inmuebleDAO;
    private final GestorDisponibilidad gestorDisponibilidad;
    private final ReservaDAO reservaDAO;
    private final GestorNotificaciones notificacion;

    public PropiedadController(InmuebleDAO inmuebleDAO,
                              GestorDisponibilidad gestorDisponibilidad,
                              ReservaDAO reservaDAO,
                              GestorNotificaciones notificacion) {
        this.inmuebleDAO = inmuebleDAO;
        this.gestorDisponibilidad = gestorDisponibilidad;
        this.reservaDAO = reservaDAO;
        this.notificacion = notificacion;
    }

    @GetMapping("/catalogo")
    public String verCatalogo(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Integer capacidad,
            Model model) {
        
        try {
            List<Inmueble> propiedadesFiltradas = obtenerPropiedadesFiltradas(ciudad, fechaInicio, fechaFin, capacidad);

            // Pasar los filtros aplicados para mostrarlos en la vista
            model.addAttribute(ATTR_PROPIEDADES, propiedadesFiltradas);
            model.addAttribute(ATTR_FILTRO_CIUDAD, ciudad);
            model.addAttribute(ATTR_FILTRO_CAPACIDAD, capacidad);
            model.addAttribute(ATTR_FILTRO_FECHA_INICIO, fechaInicio);
            model.addAttribute(ATTR_FILTRO_FECHA_FIN, fechaFin);

            if (propiedadesFiltradas.isEmpty()) {
                model.addAttribute(ATTR_MENSAJE, "No se encontraron propiedades disponibles con los criterios especificados");
            }

            return VIEW_LISTA_PROPIEDADES;
        } catch (IllegalArgumentException e) {
            model.addAttribute(ATTR_ERROR, e.getMessage());
            return VIEW_ERROR_CATALOGO;
        }
    }

    private List<Inmueble> obtenerPropiedadesFiltradas(String ciudad, String fechaInicio, String fechaFin, 
                                                       Integer capacidad) {
        // Obtenemos todos los inmuebles de la base de datos
        List<Inmueble> todasLasPropiedades = inmuebleDAO.findAll();
        
        // FILTRAR: Solo propiedades que tengan propietario (evitar propiedades eliminadas)
        List<Inmueble> propiedadesValidas = todasLasPropiedades.stream()
                .filter(inmueble -> inmueble.getPropietario() != null)
                .toList();
        
        // Filtrar según los parámetros recibidos
        List<Inmueble> propiedadesFiltradas = new ArrayList<>(propiedadesValidas);
        
        if (fechaInicio != null && !fechaInicio.isEmpty() && 
            fechaFin != null && !fechaFin.isEmpty()) {
            propiedadesFiltradas = filtrarPorFechas(propiedadesFiltradas, fechaInicio, fechaFin);
        }

        if (ciudad != null && !ciudad.isEmpty()) {
            propiedadesFiltradas = propiedadesFiltradas.stream()
                .filter(inmueble -> inmueble.getCiudad() != null && 
                        inmueble.getCiudad().toLowerCase().contains(ciudad.toLowerCase()))
                .toList();
        }

        if (capacidad != null && capacidad > 0) {
            propiedadesFiltradas = propiedadesFiltradas.stream()
                .filter(inmueble -> inmueble.getCapacidad() >= capacidad)
                .toList();
        }

        return propiedadesFiltradas;
    }

    private List<Inmueble> filtrarPorFechas(List<Inmueble> propiedades, String fechaInicio, String fechaFin) {
        try {
            LocalDate inicio = LocalDate.parse(fechaInicio);
            LocalDate fin = LocalDate.parse(fechaFin);
            Date sqlInicio = Date.valueOf(inicio);
            Date sqlFin = Date.valueOf(fin);

            // Validar fechas
            if (inicio.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de inicio no puede ser en el pasado");
            }

            if (fin.isBefore(inicio) || fin.isEqual(inicio)) {
                throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada");
            }

            // Filtrar solo los que estén disponibles en las fechas solicitadas
            return propiedades.stream()
                .filter(inmueble -> gestorDisponibilidad.verificarDisponibilidad(inmueble.getId(), sqlInicio, sqlFin))
                .toList();

        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido: " + e.getMessage());
        }
    }

    @PostMapping("/propiedades/{id}/eliminar")
    @Transactional
    public String eliminarPropiedad(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        try {
            Inmueble inmueble = inmuebleDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));
            
            // Verificar que el usuario es el propietario
            if (!inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "No tienes permiso para eliminar esta propiedad");
                return VIEW_ERROR_ELIMINAR;
            }
            
            // Obtener reservas futuras
            List<Reserva> reservasFuturas = reservaDAO.findReservasFuturasByInmueble(id);
            
            // NOTIFICAR ELIMINACIÓN
            notificacion.notificarEliminacionPropiedad(inmueble, reservasFuturas);
            
            // Reembolsar pagos
            for (Reserva reserva : reservasFuturas) {
                notificacion.crearNotificacionPagoDevuelto(reserva, reserva.getPrecioTotal());
            }
            
            // Eliminar el inmueble
            inmuebleDAO.delete(inmueble);
            
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE, "Propiedad eliminada correctamente. Se han notificado a los inquilinos afectados.");
            return REDIRECT_HOME;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, "Error al eliminar propiedad: " + e.getMessage());
            return VIEW_ERROR_ELIMINAR;
        }
    }
}

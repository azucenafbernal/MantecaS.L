package com.mantecasl.accommodationapp.business.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private static final String ATTR_COMODIDADES_DISPONIBLES = "comodidadesDisponibles";
    private static final String ATTR_POLITICAS_CANCELACION = "politicasCancelacion";
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
            @RequestParam(required = false) boolean reservaInmediata,
            @RequestParam(required = false) List<String> comodidades,
            @RequestParam(required = false) String politicaCancelacion,
            @RequestParam(required = false) Double precioMinimo,
            @RequestParam(required = false) Double precioMaximo,
            @RequestParam(required = false) boolean reservaDirecta,
            @RequestParam(required = false) boolean reservaConfirmacion,
            Model model) {
        
        try {
            List<Inmueble> propiedadesFiltradas = obtenerPropiedadesFiltradas(
                    ciudad, fechaInicio, fechaFin, capacidad, 
                    reservaInmediata, comodidades, politicaCancelacion, 
                    precioMinimo, precioMaximo, reservaDirecta, reservaConfirmacion);

            // Pasar los filtros aplicados para mostrarlos en la vista
            model.addAttribute(ATTR_PROPIEDADES, propiedadesFiltradas);
            model.addAttribute(ATTR_FILTRO_CIUDAD, ciudad);
            model.addAttribute(ATTR_FILTRO_CAPACIDAD, capacidad);
            model.addAttribute(ATTR_FILTRO_FECHA_INICIO, fechaInicio);
            model.addAttribute(ATTR_FILTRO_FECHA_FIN, fechaFin);
            
            // Pasar opciones disponibles para los filtros avanzados
            model.addAttribute(ATTR_COMODIDADES_DISPONIBLES, obtenerComodidadesDisponibles());
            model.addAttribute(ATTR_POLITICAS_CANCELACION, obtenerPoliticasCancelacion());

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
                                                       Integer capacidad, boolean reservaInmediata,
                                                       List<String> comodidades, String politicaCancelacion,
                                                       Double precioMinimo, Double precioMaximo,
                                                       boolean reservaDirecta, boolean reservaConfirmacion) {
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
        
        // Aplicar filtros avanzados
        propiedadesFiltradas = aplicarFiltrosAvanzados(propiedadesFiltradas, reservaInmediata, 
                                                       comodidades, politicaCancelacion, 
                                                       precioMinimo, precioMaximo,
                                                       reservaDirecta, reservaConfirmacion);

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
    
    /**
     * Aplica los filtros avanzados a una lista de inmuebles
     */
    private List<Inmueble> aplicarFiltrosAvanzados(List<Inmueble> propiedades, 
                                                   boolean reservaInmediata,
                                                   List<String> comodidades, 
                                                   String politicaCancelacion,
                                                   Double precioMinimo, 
                                                   Double precioMaximo,
                                                   boolean reservaDirecta,
                                                   boolean reservaConfirmacion) {
        List<Inmueble> resultado = new ArrayList<>(propiedades);
        
        // Filtro 1: Reserva inmediata
        if (reservaInmediata) {
            resultado = resultado.stream()
                .filter(Inmueble::isReservaDirecta)
                .toList();
        }
        
        // Filtro 2: Precio mínimo
        if (precioMinimo != null && precioMinimo > 0) {
            resultado = resultado.stream()
                .filter(inmueble -> inmueble.getPrecioNoche() >= precioMinimo)
                .toList();
        }
        
        // Filtro 3: Precio máximo
        if (precioMaximo != null && precioMaximo > 0) {
            resultado = resultado.stream()
                .filter(inmueble -> inmueble.getPrecioNoche() <= precioMaximo)
                .toList();
        }
        
        // Filtro 4: Comodidades
        if (comodidades != null && !comodidades.isEmpty()) {
            resultado = resultado.stream()
                .filter(inmueble -> {
                    List<String> comodidadesInmueble = inmueble.getComodidades();
                    if (comodidadesInmueble == null || comodidadesInmueble.isEmpty()) {
                        return false;
                    }
                    // Verificar que contiene al menos una de las buscadas
                    return comodidades.stream()
                        .anyMatch(c -> comodidadesInmueble.stream()
                            .anyMatch(cl -> cl.trim().equalsIgnoreCase(c.trim())));
                })
                .collect(Collectors.toList());
        }
        
        // Filtro 5: Política de cancelación
        if (politicaCancelacion != null && !politicaCancelacion.isEmpty()) {
            resultado = resultado.stream()
                .filter(inmueble -> {
                    String policyInmueble = inmueble.getPoliticaCancelacion();
                    if (policyInmueble == null || policyInmueble.isEmpty()) {
                        return false;
                    }
                    // Comparar exactamente el valor guardado
                    return policyInmueble.equalsIgnoreCase(politicaCancelacion);
                })
                .toList();
        }
        
        // Filtro 6: Tipo de Reserva
        if (reservaDirecta || reservaConfirmacion) {
            resultado = resultado.stream()
                .filter(inmueble -> {
                    boolean esReservaDirecta = inmueble.isReservaDirecta();
                    // Si ambas están seleccionadas, mostrar todas
                    if (reservaDirecta && reservaConfirmacion) {
                        return true;
                    }
                    // Si solo reserva directa está seleccionada
                    if (reservaDirecta) {
                        return esReservaDirecta;
                    }
                    // Si solo requiere confirmación está seleccionada
                    if (reservaConfirmacion) {
                        return !esReservaDirecta;
                    }
                    return false;
                })
                .toList();
        }
        
        return resultado;
    }
    
    /**
     * Obtiene las comodidades disponibles para los filtros avanzados
     */
    private List<String> obtenerComodidadesDisponibles() {
        return List.of(
            "WiFi",
            "Aire acondicionado",
            "Calefacción",
            "Piscina",
            "Jardín",
            "Parking",
            "Cocina equipada",
            "Lavadora",
            "Televisión",
            "Mascotas permitidas",
            "Acceso para personas con movilidad reducida",
            "Gimnasio"
        );
    }
    
    /**
     * Obtiene las políticas de cancelación disponibles
     */
    private List<String> obtenerPoliticasCancelacion() {
        return List.of(
            "Flexible - Cancelación gratuita hasta 7 días antes del check-in",
            "Moderada - Cancelación gratuita hasta 30 días antes del check-in",
            "Estricta - Cancelación gratuita hasta 60 días antes del check-in",
            "No reembolsable - Sin devoluciones",
            "Flexible moderada - Cancelación gratuita hasta 48 horas antes del check-in"
        );
    }
}

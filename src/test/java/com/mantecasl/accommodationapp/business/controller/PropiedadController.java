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

    private static class FiltrosAvanzados {
        List<String> comodidades;
        String politicaCancelacion;
        Double precioMinimo;
        Double precioMaximo;
        Boolean reservaDirecta;
        Boolean reservaConfirmacion;
    }

    @GetMapping("/catalogo")
    public String verCatalogo(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Integer capacidad,
            @RequestParam(required = false) List<String> comodidades,
            @RequestParam(required = false) String politicaCancelacion,
            @RequestParam(required = false) Double precioMinimo,
            @RequestParam(required = false) Double precioMaximo,
            @RequestParam(required = false) Boolean reservaDirecta,
            @RequestParam(required = false) Boolean reservaConfirmacion,
            Model model) {

        try {
            FiltrosAvanzados filtros = new FiltrosAvanzados();
            filtros.comodidades = comodidades;
            filtros.politicaCancelacion = politicaCancelacion;
            filtros.precioMinimo = precioMinimo;
            filtros.precioMaximo = precioMaximo;
            filtros.reservaDirecta = reservaDirecta;
            filtros.reservaConfirmacion = reservaConfirmacion;

            List<Inmueble> propiedadesFiltradas = obtenerPropiedadesFiltradas(
                    ciudad, fechaInicio, fechaFin, capacidad, filtros);

            model.addAttribute(ATTR_PROPIEDADES, propiedadesFiltradas);
            model.addAttribute(ATTR_FILTRO_CIUDAD, ciudad);
            model.addAttribute(ATTR_FILTRO_CAPACIDAD, capacidad);
            model.addAttribute(ATTR_FILTRO_FECHA_INICIO, fechaInicio);
            model.addAttribute(ATTR_FILTRO_FECHA_FIN, fechaFin);

            model.addAttribute(ATTR_COMODIDADES_DISPONIBLES, obtenerComodidadesDisponibles());
            model.addAttribute(ATTR_POLITICAS_CANCELACION, obtenerPoliticasCancelacion());

            if (propiedadesFiltradas.isEmpty()) {
                model.addAttribute(ATTR_MENSAJE,
                        "No se encontraron propiedades disponibles con los criterios especificados");
            }

            return VIEW_LISTA_PROPIEDADES;

        } catch (RuntimeException e) { // 🔧 CAMBIO CLAVE
            model.addAttribute(ATTR_ERROR, e.getMessage());
            return VIEW_ERROR_CATALOGO;
        }
    }

    private List<Inmueble> obtenerPropiedadesFiltradas(
            String ciudad,
            String fechaInicio,
            String fechaFin,
            Integer capacidad,
            FiltrosAvanzados filtrosAvanzados) {

        List<Inmueble> propiedades = inmuebleDAO.findAll().stream()
                .filter(i -> i.getPropietario() != null)
                .toList();

        List<Inmueble> resultado = new ArrayList<>(propiedades);

        if (fechaInicio != null && fechaFin != null
                && !fechaInicio.isEmpty() && !fechaFin.isEmpty()) {
            resultado = filtrarPorFechas(resultado, fechaInicio, fechaFin);
        }

        if (ciudad != null && !ciudad.isEmpty()) {
            resultado = resultado.stream()
                    .filter(i -> i.getCiudad() != null
                            && i.getCiudad().toLowerCase().contains(ciudad.toLowerCase()))
                    .toList();
        }

        if (capacidad != null && capacidad > 0) {
            resultado = resultado.stream()
                    .filter(i -> i.getCapacidad() >= capacidad)
                    .toList();
        }

        return aplicarFiltrosAvanzados(
                resultado,
                filtrosAvanzados.comodidades,
                filtrosAvanzados.politicaCancelacion,
                filtrosAvanzados.precioMinimo,
                filtrosAvanzados.precioMaximo,
                filtrosAvanzados.reservaDirecta,
                filtrosAvanzados.reservaConfirmacion);
    }

    private List<Inmueble> filtrarPorFechas(List<Inmueble> propiedades,
            String fechaInicio,
            String fechaFin) {

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);

        if (inicio.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser en el pasado");
        }

        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior a la de entrada");
        }

        Date sqlInicio = Date.valueOf(inicio);
        Date sqlFin = Date.valueOf(fin);

        return propiedades.stream()
                .filter(i -> gestorDisponibilidad.verificarDisponibilidad(
                        i.getId(), sqlInicio, sqlFin))
                .toList();
    }

    private List<Inmueble> aplicarFiltrosAvanzados(
            List<Inmueble> propiedades,
            List<String> comodidades,
            String politicaCancelacion,
            Double precioMinimo,
            Double precioMaximo,
            Boolean reservaDirecta,
            Boolean reservaConfirmacion) {

        List<Inmueble> resultado = new ArrayList<>(propiedades);

        resultado = filtrarPorPrecioMinimo(resultado, precioMinimo);
        resultado = filtrarPorPrecioMaximo(resultado, precioMaximo);
        resultado = filtrarPorComodidades(resultado, comodidades);
        resultado = filtrarPorPoliticaCancelacion(resultado, politicaCancelacion);
        resultado = filtrarPorTipoReserva(resultado, reservaDirecta, reservaConfirmacion);

        return resultado;
    }

    private List<Inmueble> filtrarPorPrecioMinimo(List<Inmueble> propiedades, Double precioMinimo) {
        if (precioMinimo == null || precioMinimo <= 0) {
            return propiedades;
        }
        return propiedades.stream()
                .filter(i -> i.getPrecioNoche() >= precioMinimo)
                .toList();
    }

    private List<Inmueble> filtrarPorPrecioMaximo(List<Inmueble> propiedades, Double precioMaximo) {
        if (precioMaximo == null || precioMaximo <= 0) {
            return propiedades;
        }
        return propiedades.stream()
                .filter(i -> i.getPrecioNoche() <= precioMaximo)
                .toList();
    }

    private List<Inmueble> filtrarPorComodidades(List<Inmueble> propiedades, List<String> comodidades) {
        if (comodidades == null || comodidades.isEmpty()) {
            return propiedades;
        }
        return propiedades.stream()
                .filter(i -> contieneAlgunaComodidad(i, comodidades))
                .toList();
    }

    private boolean contieneAlgunaComodidad(Inmueble inmueble, List<String> comodidades) {
        List<String> comodidadesInmueble = inmueble.getComodidades();
        if (comodidadesInmueble == null || comodidadesInmueble.isEmpty()) {
            return false;
        }
        return comodidades.stream()
                .anyMatch(c -> comodidadesInmueble.stream()
                        .anyMatch(ci -> ci.trim().equalsIgnoreCase(c.trim())));
    }

    private List<Inmueble> filtrarPorPoliticaCancelacion(
            List<Inmueble> propiedades,
            String politicaCancelacion) {

        if (politicaCancelacion == null || politicaCancelacion.isEmpty()) {
            return propiedades;
        }
        return propiedades.stream()
                .filter(i -> politicaCancelacion.equalsIgnoreCase(
                        i.getPoliticaCancelacion()))
                .toList();
    }

    private List<Inmueble> filtrarPorTipoReserva(
            List<Inmueble> propiedades,
            Boolean reservaDirecta,
            Boolean reservaConfirmacion) {

        boolean filtrarDirecta = Boolean.TRUE.equals(reservaDirecta);
        boolean filtrarConfirmacion = Boolean.TRUE.equals(reservaConfirmacion);

        if (filtrarDirecta == filtrarConfirmacion) {
            return propiedades;
        }

        return propiedades.stream()
                .filter(i -> i.isReservaDirecta() == filtrarDirecta)
                .toList();
    }

    @PostMapping("/propiedades/{id}/eliminar")
    @Transactional
    public String eliminarPropiedad(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);

        try {
            Inmueble inmueble = inmuebleDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));

            if (!inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(
                        ATTR_ERROR, "No tienes permiso para eliminar esta propiedad");
                return VIEW_ERROR_ELIMINAR;
            }

            List<Reserva> reservasFuturas = reservaDAO.findReservasFuturasByInmueble(id);

            notificacion.notificarEliminacionPropiedad(inmueble, reservasFuturas);

            for (Reserva reserva : reservasFuturas) {
                notificacion.crearNotificacionPagoDevuelto(
                        reserva, reserva.getPrecioTotal());
            }

            inmuebleDAO.delete(inmueble);

            redirectAttributes.addFlashAttribute(
                    ATTR_MENSAJE,
                    "Propiedad eliminada correctamente. Se han notificado a los inquilinos afectados.");

            return REDIRECT_HOME;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    ATTR_ERROR, "Error al eliminar propiedad: " + e.getMessage());
            return VIEW_ERROR_ELIMINAR;
        }
    }

    private List<String> obtenerComodidadesDisponibles() {
        return List.of(
                "WiFi", "Aire acondicionado", "Calefacción", "Piscina",
                "Jardín", "Parking", "Cocina equipada", "Lavadora",
                "Televisión", "Mascotas permitidas",
                "Acceso para personas con movilidad reducida", "Gimnasio");
    }

    private List<String> obtenerPoliticasCancelacion() {
        return List.of(
                "Flexible - Cancelación gratuita hasta 7 días antes del check-in",
                "Moderada - Cancelación gratuita hasta 30 días antes del check-in",
                "Estricta - Cancelación gratuita hasta 60 días antes del check-in",
                "No reembolsable - Sin devoluciones",
                "Flexible moderada - Cancelación gratuita hasta 48 horas antes del check-in");
    }
}

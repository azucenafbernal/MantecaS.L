package com.mantecasl.accommodationapp.business.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

@Service
public class GestorDisponibilidad {

    @Autowired
    private DisponibilidadDAO disponibilidadDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    // Validar fechas
    public Date[] validarFechas(String fechaInicio, String fechaFin) {
        if (fechaInicio == null || fechaFin == null ||
                fechaInicio.isEmpty() || fechaFin.isEmpty()) {
            throw new RuntimeException("Debes seleccionar las fechas de entrada y salida.");
        }

        try {
            LocalDate inicio = LocalDate.parse(fechaInicio);
            LocalDate fin = LocalDate.parse(fechaFin);

            if (fin.isBefore(inicio) || fin.equals(inicio)) {
                throw new RuntimeException("La fecha de salida debe ser posterior a la de entrada.");
            }

            return new Date[]{
                    Date.valueOf(inicio),
                    Date.valueOf(fin)
            };

        } catch (Exception e) {
            throw new RuntimeException("Formato de fecha inválido: " + e.getMessage());
        }
    }

    // Crear reserva (disponibilidad con disponible=false)
    public Disponibilidad crearReserva(Long inmuebleId, Date inicio, Date fin, boolean esDirecta) {
        // Verificar que el inmueble existe
        Inmueble inmueble = inmuebleDAO.findById(inmuebleId)
                .orElseThrow(() -> new RuntimeException("Inmueble no encontrado."));

        // Verificar que no hay solapamientos con reservas existentes
        if (!verificarDisponibilidad(inmuebleId, inicio, fin)) {
            throw new RuntimeException("El inmueble no está disponible en las fechas seleccionadas.");
        }

        // Crear la disponibilidad (reserva)
        Disponibilidad reserva = new Disponibilidad();
        reserva.setInmueble(inmueble);
        reserva.setFechaInicio(inicio);
        reserva.setFechaFin(fin);
        reserva.setDirecta(esDirecta);
        reserva.setDisponible(false); 

        return disponibilidadDAO.save(reserva);
    }

    // Verificar disponibilidad
    public boolean verificarDisponibilidad(Long inmuebleId, Date inicio, Date fin) {
        List<Disponibilidad> reservasExistentes = disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(inmuebleId);
        
        for (Disponibilidad reserva : reservasExistentes) {
            if (seSolapan(reserva.getFechaInicio(), reserva.getFechaFin(), inicio, fin)) {
                return false;
            }
        }
        return true;
    }

    // Método auxiliar para verificar solapamiento
    private boolean seSolapan(Date inicio1, Date fin1, Date inicio2, Date fin2) {
        return (inicio1.before(fin2) && inicio2.before(fin1));
    }

    // Buscar inmuebles disponibles
    public List<Inmueble> buscarInmueblesDisponibles(Date fechaInicio, Date fechaFin, String ciudad, Integer capacidad) {
        List<Inmueble> todosInmuebles = inmuebleDAO.findAll();
        List<Inmueble> disponibles = new ArrayList<>();

        for (Inmueble inmueble : todosInmuebles) {
            // Filtro de ciudad
            boolean pasaCiudad = true;
            if (ciudad != null && !ciudad.isEmpty()) {
                pasaCiudad = inmueble.getCiudad().toLowerCase().contains(ciudad.toLowerCase());
            }

            // Filtro de capacidad
            boolean pasaCapacidad = true;
            if (capacidad != null) {
                pasaCapacidad = inmueble.getCapacidad() >= capacidad;
            }

            // Verificar disponibilidad de fechas
            boolean estaDisponible = verificarDisponibilidad(inmueble.getId(), fechaInicio, fechaFin);

            if (pasaCiudad && pasaCapacidad && estaDisponible) {
                disponibles.add(inmueble);
            }
        }

        return disponibles;
    }

    // Obtener reservas de un inmueble
    public List<Disponibilidad> obtenerReservasPorInmueble(Long inmuebleId) {
        return disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(inmuebleId);
    }
}
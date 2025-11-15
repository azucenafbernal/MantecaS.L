package com.mantecasl.accommodationapp.business.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import jakarta.transaction.Transactional;

@Controller
@Transactional
public class GestorDisponibilidad {
    @Autowired
    private DisponibilidadDAO disponibilidadDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    //Creamos la disponibilidad
    public Disponibilidad crearDisponibilidad(Long inmuebleId, Date fechaInicio, Date fechaFin, double precio, boolean directa){
        Optional<Inmueble> inmuebleOpt = inmuebleDAO.findById(inmuebleId);
        if (!inmuebleOpt.isPresent()) {
            throw new RuntimeException("Inmueble no encontrado con ID: " + inmuebleId);
        }
        Inmueble inmueble = inmuebleOpt.get();

        Disponibilidad disponibilidad = new Disponibilidad(inmueble, fechaInicio, fechaFin, precio, directa);
        return disponibilidadDAO.save(disponibilidad);
    }

    //Verificar disponibilidad
    public boolean verificarDisponibilidad(Long inmuebleId, Date fechaInicio, Date fechaFin){
        List<Disponibilidad> disponibilidades = disponibilidadDAO.findDisponiblesEnRango(inmuebleId, fechaInicio, fechaFin);
    
        return !disponibilidades.isEmpty();
    }

    public Date[] validarFechas(String fechaInicioStr, String fechaFinStr) {
        LocalDate fechaInicio;
        LocalDate fechaFin;
        
        // Si no vienen fechas, usar valores por defecto
        if (fechaInicioStr == null || fechaInicioStr.isEmpty()) {
            fechaInicio = LocalDate.now();
        } else {
            fechaInicio = LocalDate.parse(fechaInicioStr);
        }
        
        if (fechaFinStr == null || fechaFinStr.isEmpty()) {
            fechaFin = LocalDate.now().plusDays(1);
        } else {
            fechaFin = LocalDate.parse(fechaFinStr);
        }
        
        // Validar que la fecha fin sea despues de la inicio
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new RuntimeException("La fecha de salida debe ser posterior a la de entrada");
        }
        
        // Convertir a sql.Date
        Date fechaInicioSql = Date.valueOf(fechaInicio);
        Date fechaFinSql = Date.valueOf(fechaFin);
        
        return new Date[]{fechaInicioSql, fechaFinSql};
    }

    //Buscar todos los inmuebles disponibles
    public List<Inmueble> buscarInmueblesDisponibles(Date fechaInicio, Date fechaFin, String ciudad, Integer capacidad) {
        List<Inmueble> todosInmuebles = inmuebleDAO.findAll();
        List<Inmueble> inmueblesDisponibles = new ArrayList<>();

        for (Inmueble inmueble : todosInmuebles) {
            // Verificar filtro de ciudad
            boolean pasaFiltroCiudad = true;
            if (ciudad != null && !ciudad.isEmpty()) {
                String direccionLower = inmueble.getDireccion().toLowerCase();
                String ciudadLower = ciudad.toLowerCase();
                pasaFiltroCiudad = direccionLower.contains(ciudadLower);
            }

            // Verificar filtro de capacidad
            boolean pasaFiltroCapacidad = true;
            if (capacidad != null) {
                pasaFiltroCapacidad = inmueble.getCapacidad() >= capacidad;
            }

            // Verificar disponibilidad
            boolean estaDisponible = verificarDisponibilidad(inmueble.getId(), fechaInicio, fechaFin);

            // Si pasa todos los filtros, agregar a la lista
            if (pasaFiltroCiudad && pasaFiltroCapacidad && estaDisponible) {
                inmueblesDisponibles.add(inmueble);
            }
        }

        return inmueblesDisponibles;
    }

    //Bloquear disponibilidad
    public void bloquearDisponibilidad(Long inmuebleId, Date fechaInicio, Date fechaFin){
        List<Disponibilidad> disponibilidades = disponibilidadDAO.findDisponiblesEnRango(inmuebleId, fechaInicio, fechaFin);
        
        //cambiar esta parte
        for(Disponibilidad disp : disponibilidades){
            disp.setDisponible(false);
            disponibilidadDAO.save(disp);          
        }
    }

    //Liberar disponibilidad
    public void liberarDisponibilidad(Long inmuebleId, Date fechaInicio, Date fechaFin){
        List<Disponibilidad> disponibilidades = disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(inmuebleId);

        for(Disponibilidad disp: disponibilidades){
            if(disp.esValida(fechaInicio) && disp.esValida(fechaFin)){
                disp.setDisponible(true);
                disponibilidadDAO.save(disp);
            }
        }
    }

    //Obtener disponibilidad de un inmueble
    public List<Disponibilidad> obtenerDisponibilidadesPorInmueble(Long inmuebleId){
        return disponibilidadDAO.findByInmuebleId(inmuebleId);
    }
    
}

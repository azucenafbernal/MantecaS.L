package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GestorReservas {

    @Autowired
    private SolicitudReservaDAO solicitudReservaDAO;

    @Autowired
    private ReservaDAO reservaDAO;

    //PENDIENTES
    public List<SolicitudReserva> obtenerSolicitudesPendientesPropietario(Long propietarioId) {
        return solicitudReservaDAO.findByInmueblePropietarioIdAndEstado(propietarioId, "PENDIENTE");
    }

    //APROBADAS
    public List<SolicitudReserva> obtenerSolicitudesAprobadasPropietario(Long propietarioId) {
        return solicitudReservaDAO.findByInmueblePropietarioIdAndEstado(propietarioId, "APROBADA");
    }

    //RECHAZADAS
    public List<SolicitudReserva> obtenerSolicitudesRechazadasPropietario(Long propietarioId) {
        return solicitudReservaDAO.findByInmueblePropietarioIdAndEstado(propietarioId, "RECHAZADA");
    }

    //HISTORIAL
    public List<Reserva> obtenerHistorialReservasPropietario(Long propietarioId) {
        List<Reserva> todas = reservaDAO.findAll()
                .stream()
                .filter(r -> r.getInmueble().getPropietario().getId().equals(propietarioId))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .limit(10)
                .toList();

        return todas;
    }
}

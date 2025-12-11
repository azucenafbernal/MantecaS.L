package com.mantecasl.accommodationapp.business.controller;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.entity.SolicitudReserva;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;
import com.mantecasl.accommodationapp.business.persistance.SolicitudReservaDAO;

@Service
@Transactional
public class GestorReservas {

    private SolicitudReservaDAO solicitudReservaDAO;
    private ReservaDAO reservaDAO;

    public GestorReservas(SolicitudReservaDAO solicitudReservaDAO, ReservaDAO reservaDAO) {
        this.solicitudReservaDAO = solicitudReservaDAO;
        this.reservaDAO = reservaDAO;
    }

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
        return reservaDAO.findAll()
            .stream()
            .filter(r -> r.getInmueble().getPropietario().getId().equals(propietarioId))
            .sorted((a, b) -> b.getId().compareTo(a.getId()))
            .limit(10)
            .toList();
    }
}

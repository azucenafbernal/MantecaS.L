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

    public GestorReservas(SolicitudReservaDAO solicitudReservaDAO,
                          ReservaDAO reservaDAO) {
        this.solicitudReservaDAO = solicitudReservaDAO;
        this.reservaDAO = reservaDAO;
    }

    //Pendientes
    public List<SolicitudReserva> obtenerSolicitudesPendientesPropietario(Long usuarioId) {
        return solicitudReservaDAO
                .findByInmueblePropietarioUsuarioIdAndEstado(usuarioId, "PENDIENTE");
    }

    //Aprobadas
    public List<SolicitudReserva> obtenerSolicitudesAprobadasPropietario(Long usuarioId) {
        return solicitudReservaDAO
                .findByInmueblePropietarioUsuarioIdAndEstado(usuarioId, "APROBADA");
    }

    //Rechazadas
    public List<SolicitudReserva> obtenerSolicitudesRechazadasPropietario(Long usuarioId) {
        return solicitudReservaDAO
                .findByInmueblePropietarioUsuarioIdAndEstado(usuarioId, "RECHAZADA");
    }

    //Historial reciente
    public List<Reserva> obtenerHistorialReservasPropietario(Long usuarioId) {
        return reservaDAO
                .findTop10ByInmueblePropietarioUsuarioIdOrderByIdDesc(usuarioId);
    }
}
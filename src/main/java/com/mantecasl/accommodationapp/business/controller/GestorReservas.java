package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.entity.SolicitudReserva;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;
import com.mantecasl.accommodationapp.business.persistance.SolicitudReservaDAO;
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

/*package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GestorReservas {

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private InquilinoDAO inquilinoDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    //Crear una nueva reserva
    public Reserva crearReserva(
            Long inmuebleId,
            Long usuarioId,
            Date fechaInicio,
            Date fechaFin,
            String telefono,
            String documentoIdentidad,
            String metodoPago,
            String observaciones
    ) {

        // Verificar inmueble
        Inmueble inmueble = inmuebleDAO.findById(inmuebleId)
                .orElseThrow(() -> new RuntimeException("Inmueble no encontrado"));

        // Verificar usuario
        Usuario usuario = usuarioDAO.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar disponibilidad
        if (!verificarDisponibilidad(inmuebleId, fechaInicio, fechaFin)) {
            throw new RuntimeException("El inmueble no está disponible en esas fechas");
        }

        // Buscar o crear inquilino
        Inquilino inquilino = inquilinoDAO.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Inquilino nuevo = new Inquilino(usuario, telefono, documentoIdentidad, metodoPago);
                    nuevo.setMetodoPago(metodoPago);
                    return inquilinoDAO.save(nuevo);
                });

        // Si el inquilino ya existía, actualizar datos
        inquilino.setTelefono(telefono);
        inquilino.setDocumentoIdentidad(documentoIdentidad);
        inquilino.setMetodoPago(metodoPago);
        inquilinoDAO.save(inquilino);

        // Calcular precio
        double precioTotal = calcularPrecioTotal(inmueble, fechaInicio, fechaFin);

        // Crear reserva
        Reserva reserva = new Reserva(
                inmueble,
                inquilino,
                fechaInicio,
                fechaFin,
                precioTotal
        );

        reserva.setObservaciones(observaciones);

        return reservaDAO.save(reserva);
    }

    // Verificar disponibilidad
    public boolean verificarDisponibilidad(Long inmuebleId, Date fechaInicio, Date fechaFin) {
        List<Reserva> reservasActivas =
                reservaDAO.findReservasActivasEnRango(inmuebleId, fechaInicio, fechaFin);
        return reservasActivas.isEmpty();
    }

    //Calcular precio total
    private double calcularPrecioTotal(Inmueble inmueble, Date fechaInicio, Date fechaFin) {

        long dias = ChronoUnit.DAYS.between(
                fechaInicio.toLocalDate(),
                fechaFin.toLocalDate()
        );

        if (dias <= 0) {
            throw new RuntimeException("Las fechas no son válidas");
        }

        return inmueble.getPrecioNoche() * dias;
    }

    //Confirmar reserva
    public Reserva confirmarReserva(Long reservaId) {
        Reserva reserva = reservaDAO.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reserva.confirmar();
        return reservaDAO.save(reserva);
    }

    //Cancelar reserva
    public Reserva cancelarReserva(Long reservaId, String motivo) {
        Reserva reserva = reservaDAO.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reserva.cancelar(motivo);
        return reservaDAO.save(reserva);
    }

    //Obtener reservas por usuario
    public List<Reserva> obtenerReservasPorUsuario(Long usuarioId) {
        return reservaDAO.findByUsuarioId(usuarioId);
    }

    //Obtener reservas por inmueble
    public List<Reserva> obtenerReservasPorInmueble(Long inmuebleId) {
        return reservaDAO.findByInmuebleId(inmuebleId);
    }

    //Obtener inquilino por usuario
    public Optional<Inquilino> obtenerInquilinoPorUsuario(Long usuarioId) {
        return inquilinoDAO.findByUsuarioId(usuarioId);
    }
}*/

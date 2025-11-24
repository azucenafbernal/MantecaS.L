package com.mantecasl.accommodationapp.business.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.persistance.*;


@Service
public class GestorReservas {
    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    public void crearReserva(Long inmuebleId, LocalDate inicio, LocalDate fin) throws Exception {

        Inmueble inmueble = inmuebleDAO.findById(inmuebleId).orElse(null);
        if (inmueble == null) {
            throw new Exception("El inmueble no existe.");
        }

        // Comprobar solapamiento de fechas
        List<Reserva> reservasSolapadas =
                reservaDAO.findByInmuebleIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        inmuebleId, fin, inicio);

        if (!reservasSolapadas.isEmpty()) {
            throw new Exception("El inmueble no está disponible en las fechas seleccionadas.");
        }

        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setInmueble(inmueble);
        reserva.setFechaInicio(inicio);
        reserva.setFechaFin(fin);

        reservaDAO.save(reserva);
    }
}

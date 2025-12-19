package com.mantecasl.accommodationapp.business.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;
import com.mantecasl.accommodationapp.business.persistance.SolicitudReservaDAO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestorReservasTest {

    @Mock
    private SolicitudReservaDAO solicitudReservaDAO;

    @Mock
    private ReservaDAO reservaDAO;

    @InjectMocks
    private GestorReservas gestorReservas;

    @Test
    void obtenerSolicitudesPendientesPropietario_devuelve_lista() {
        when(solicitudReservaDAO
                .findByInmueblePropietarioIdAndEstado(1L, "PENDIENTE"))
                .thenReturn(List.of(new SolicitudReserva()));

        List<SolicitudReserva> resultado = gestorReservas.obtenerSolicitudesPendientesPropietario(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerSolicitudesAprobadasPropietario_devuelve_lista() {
        when(solicitudReservaDAO
                .findByInmueblePropietarioIdAndEstado(1L, "APROBADA"))
                .thenReturn(List.of(new SolicitudReserva()));

        List<SolicitudReserva> resultado = gestorReservas.obtenerSolicitudesAprobadasPropietario(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerSolicitudesRechazadasPropietario_devuelve_lista() {
        when(solicitudReservaDAO
                .findByInmueblePropietarioIdAndEstado(1L, "RECHAZADA"))
                .thenReturn(List.of(new SolicitudReserva()));

        List<SolicitudReserva> resultado = gestorReservas.obtenerSolicitudesRechazadasPropietario(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerHistorialReservasPropietario_filtra_por_propietario_y_limita() {
        Propietario propietario = new Propietario();
        propietario.setId(1L);

        Inmueble inmueble = new Inmueble();
        inmueble.setPropietario(propietario);

        Reserva r1 = new Reserva();
        r1.setId(1L);
        r1.setInmueble(inmueble);

        Reserva r2 = new Reserva();
        r2.setId(2L);
        r2.setInmueble(inmueble);

        when(reservaDAO.findAll()).thenReturn(List.of(r1, r2));

        List<Reserva> resultado = gestorReservas.obtenerHistorialReservasPropietario(1L);

        assertEquals(2, resultado.size());
        assertEquals(2L, resultado.get(0).getId()); // orden descendente
    }
}

package com.mantecasl.accommodationapp.business.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.List;

import com.mantecasl.accommodationapp.business.entity.Disponibilidad;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.DisponibilidadDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;
import com.mantecasl.accommodationapp.business.persistance.SolicitudReservaDAO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestorDisponibilidadTest {

    @Mock
    private DisponibilidadDAO disponibilidadDAO;

    @Mock
    private ReservaDAO reservaDAO;

    @Mock
    private InmuebleDAO inmuebleDAO;

    @Mock
    private SolicitudReservaDAO solicitudReservaDAO;

    @InjectMocks
    private GestorDisponibilidad gestor;

    // ---------- VALIDAR FECHAS ----------

    @Test
    void validarFechas_correctas() {
        Date[] fechas = gestor.validarFechas("2025-01-10", "2025-01-15");

        assertEquals(Date.valueOf("2025-01-10"), fechas[0]);
        assertEquals(Date.valueOf("2025-01-15"), fechas[1]);
    }

    @Test
    void validarFechas_finAntesQueInicio_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> gestor.validarFechas("2025-01-15", "2025-01-10"));
    }

    // ---------- VERIFICAR DISPONIBILIDAD ----------

    @Test
    void verificarDisponibilidad_sinSolapamientos() {
        when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                .thenReturn(List.of());

        boolean disponible = gestor.verificarDisponibilidad(
                1L,
                Date.valueOf("2025-01-10"),
                Date.valueOf("2025-01-15"));

        assertTrue(disponible);
    }

    @Test
    void verificarDisponibilidad_conSolapamiento() {
        Disponibilidad d = new Disponibilidad();
        d.setFechaInicio(Date.valueOf("2025-01-12"));
        d.setFechaFin(Date.valueOf("2025-01-20"));

        when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                .thenReturn(List.of(d));

        boolean disponible = gestor.verificarDisponibilidad(
                1L,
                Date.valueOf("2025-01-10"),
                Date.valueOf("2025-01-15"));

        assertFalse(disponible);
    }

    // ---------- BUSCAR INMUEBLES DISPONIBLES ----------

    @Test
    void buscarInmueblesDisponibles_filtraCorrectamente() {
        Inmueble i = new Inmueble();
        i.setId(1L);
        i.setCiudad("Madrid");
        i.setCapacidad(4);

        when(inmuebleDAO.findAll()).thenReturn(List.of(i));
        when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                .thenReturn(List.of());

        List<Inmueble> resultado = gestor.buscarInmueblesDisponibles(
                Date.valueOf("2025-01-10"),
                Date.valueOf("2025-01-15"),
                "Madrid",
                2);

        assertEquals(1, resultado.size());
    }
}

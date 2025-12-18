package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;

import org.junit.jupiter.api.Test;

public class DisponibilidadTest {

    @Test
    void esValida_devuelveTrue_siFechaDentroDelRango() {
        // given
        Disponibilidad disp = new Disponibilidad();
        disp.setFechaInicio(Date.valueOf("2025-01-10"));
        disp.setFechaFin(Date.valueOf("2025-01-20"));

        Date fecha = Date.valueOf("2025-01-15");

        // when
        boolean resultado = disp.esValida(fecha);

        // then
        assertTrue(resultado);
    }

    @Test
    void esValida_devuelveTrue_siFechaIgualAlInicio() {
        Disponibilidad disp = new Disponibilidad();
        disp.setFechaInicio(Date.valueOf("2025-01-10"));
        disp.setFechaFin(Date.valueOf("2025-01-20"));

        Date fecha = Date.valueOf("2025-01-10");

        boolean resultado = disp.esValida(fecha);

        assertTrue(resultado);
    }

    @Test
    void esValida_devuelveTrue_siFechaIgualAlFin() {
        Disponibilidad disp = new Disponibilidad();
        disp.setFechaInicio(Date.valueOf("2025-01-10"));
        disp.setFechaFin(Date.valueOf("2025-01-20"));

        Date fecha = Date.valueOf("2025-01-20");

        boolean resultado = disp.esValida(fecha);

        assertTrue(resultado);
    }

    @Test
    void esValida_devuelveFalse_siFechaAntesDelInicio() {
        Disponibilidad disp = new Disponibilidad();
        disp.setFechaInicio(Date.valueOf("2025-01-10"));
        disp.setFechaFin(Date.valueOf("2025-01-20"));

        Date fecha = Date.valueOf("2025-01-05");

        boolean resultado = disp.esValida(fecha);

        assertFalse(resultado);
    }

    @Test
    void esValida_devuelveFalse_siFechaDespuesDelFin() {
        Disponibilidad disp = new Disponibilidad();
        disp.setFechaInicio(Date.valueOf("2025-01-10"));
        disp.setFechaFin(Date.valueOf("2025-01-20"));

        Date fecha = Date.valueOf("2025-01-25");

        boolean resultado = disp.esValida(fecha);

        assertFalse(resultado);
    }
}

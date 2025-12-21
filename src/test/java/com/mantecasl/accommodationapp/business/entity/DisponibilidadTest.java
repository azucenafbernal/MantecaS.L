package com.mantecasl.accommodationapp.business.entity;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DisponibilidadTest {

    @ParameterizedTest
    @ValueSource(strings = {"2025-01-10", "2025-01-15", "2025-01-20"})
    void esValida_devuelveTrue_paraFechasDentroOIgualAlRango(String fechaStr) {
        Disponibilidad disp = new Disponibilidad();
        disp.setFechaInicio(Date.valueOf("2025-01-10"));
        disp.setFechaFin(Date.valueOf("2025-01-20"));

        Date fecha = Date.valueOf(fechaStr);

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

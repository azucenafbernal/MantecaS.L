package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class NotificacionTest {

    @Test
    void constructorPorDefecto_inicializaFechaYLeidaFalse() {
        Notificacion notificacion = new Notificacion();

        assertNotNull(notificacion.getFechaCreacion());
        assertFalse(notificacion.isLeida());
    }

    @Test
    void constructorConParametros_asignaCamposCorrectamente() {
        Usuario usuario = new Usuario("Alejandro", "a@test.com", "1234");

        Notificacion notificacion = new Notificacion(
                usuario,
                "Título",
                "Mensaje",
                Notificacion.AVISO_SISTEMA);

        assertEquals(usuario, notificacion.getUsuario());
        assertEquals("Título", notificacion.getTitulo());
        assertEquals("Mensaje", notificacion.getMensaje());
        assertEquals(Notificacion.AVISO_SISTEMA, notificacion.getTipo());
    }

    @Test
    void isReciente_devuelveTrue_siMenosDe24Horas() {
        Notificacion notificacion = new Notificacion();
        notificacion.setFechaCreacion(LocalDateTime.now().minusHours(5));

        assertTrue(notificacion.isReciente());
    }

    @Test
    void isReciente_devuelveFalse_siMasDe24Horas() {
        Notificacion notificacion = new Notificacion();
        notificacion.setFechaCreacion(LocalDateTime.now().minusHours(30));

        assertFalse(notificacion.isReciente());
    }

    @Test
    void isReciente_devuelveFalse_enLimite24Horas() {
        Notificacion notificacion = new Notificacion();
        notificacion.setFechaCreacion(LocalDateTime.now().minusHours(24));

        assertFalse(notificacion.isReciente());
    }

    @Test
    void isReciente_fechaNull_lanzaExcepcion() {
        Notificacion notificacion = new Notificacion();
        notificacion.setFechaCreacion(null);

        assertThrows(NullPointerException.class, notificacion::isReciente);
    }

    @Test
    void getFechaFormateada_devuelveFormatoCorrecto() {
        Notificacion notificacion = new Notificacion();
        notificacion.setFechaCreacion(LocalDateTime.of(2025, 1, 10, 15, 30));

        String resultado = notificacion.getFechaFormateada();

        assertEquals("10/01/2025 15:30", resultado);
    }

    @Test
    void getFechaFormateada_fechaNull_lanzaExcepcion() {
        Notificacion notificacion = new Notificacion();
        notificacion.setFechaCreacion(null);

        assertThrows(NullPointerException.class, notificacion::getFechaFormateada);
    }
}

package com.mantecasl.accommodationapp.business.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReservaExceptionTest {

    @Test
    void constructor_con_mensaje() {
        // Arrange
        String mensaje = "Error en la reserva";

        // Act
        ReservaException exception = new ReservaException(mensaje);

        // Assert
        assertEquals(mensaje, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_con_mensaje_y_causa() {
        // Arrange
        String mensaje = "Error grave en la reserva";
        Throwable causa = new IllegalStateException("Causa original");

        // Act
        ReservaException exception = new ReservaException(mensaje, causa);

        // Assert
        assertEquals(mensaje, exception.getMessage());
        assertEquals(causa, exception.getCause());
    }
}

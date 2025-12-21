package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InquilinoTest {

    private Inquilino inquilino;

    @BeforeEach
    void setUp() {
        inquilino = new Inquilino();
    }

    @Test
    void actualizarDatosPago_tarjeta_guardaDatosTarjeta() {
        inquilino.actualizarDatosPago(
                "TARJETA",
                "1234567890123456",
                "12/26",
                "123",
                "paypal@test.com");

        assertEquals("TARJETA", inquilino.getMetodoPago());
        assertEquals("1234567890123456", inquilino.getNumeroTarjeta());
        assertEquals("12/26", inquilino.getFechaCaducidad());
        assertEquals("123", inquilino.getCvv());
        assertNull(inquilino.getPaypalEmail());
    }

    @Test
    void actualizarDatosPago_paypal_guardaEmailPaypal() {
        inquilino.actualizarDatosPago(
                "PAYPAL",
                "123",
                "12/26",
                "999",
                "paypal@test.com");

        assertEquals("PAYPAL", inquilino.getMetodoPago());
        assertEquals("paypal@test.com", inquilino.getPaypalEmail());
        assertNull(inquilino.getNumeroTarjeta());
        assertNull(inquilino.getFechaCaducidad());
        assertNull(inquilino.getCvv());
    }

    @Test
    void actualizarDatosPago_cambiaDeTarjetaAPaypal_limpiaDatosAntiguos() {
        inquilino.actualizarDatosPago(
                "TARJETA",
                "1111",
                "01/30",
                "321",
                null);

        inquilino.actualizarDatosPago(
                "PAYPAL",
                null,
                null,
                null,
                "nuevo@paypal.com");

        assertEquals("PAYPAL", inquilino.getMetodoPago());
        assertEquals("nuevo@paypal.com", inquilino.getPaypalEmail());
        assertNull(inquilino.getNumeroTarjeta());
        assertNull(inquilino.getFechaCaducidad());
        assertNull(inquilino.getCvv());
    }

    @Test
    void actualizarDatosPago_metodoDesconocido_noGuardaDatos() {
        inquilino.actualizarDatosPago(
                "BIZUM",
                "1111",
                "01/30",
                "321",
                "paypal@test.com");

        assertEquals("BIZUM", inquilino.getMetodoPago());
        assertNull(inquilino.getNumeroTarjeta());
        assertNull(inquilino.getPaypalEmail());
    }

    @Test
    void actualizarDatosPago_metodoNull_noLanzaExcepcion() {
        assertDoesNotThrow(() -> inquilino.actualizarDatosPago(null, null, null, null, null));
    }

    @Test
    void getNombreCompleto_conUsuario_devuelveNombre() {
        Usuario usuario = new Usuario("Alejandro", "a@test.com", "1234");
        inquilino.setUsuario(usuario);

        assertEquals("Alejandro", inquilino.getNombreCompleto());
    }

    @Test
    void getNombreCompleto_sinUsuario_devuelveCadenaVacia() {
        assertEquals("", inquilino.getNombreCompleto());
    }

    @Test
    void getEmail_conUsuario_devuelveEmail() {
        Usuario usuario = new Usuario("Alejandro", "a@test.com", "1234");
        inquilino.setUsuario(usuario);

        assertEquals("a@test.com", inquilino.getEmail());
    }

    @Test
    void getEmail_sinUsuario_devuelveCadenaVacia() {
        assertEquals("", inquilino.getEmail());
    }
}

package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReservaTest {

    private Inquilino inquilino;
    private Inmueble inmueble;

    @BeforeEach
    void setUp() {
        Usuario u = new Usuario("Pepe", "pepe@example.com", "1234");

        inquilino = new Inquilino(u, "600000000", "12345678A", "TARJETA");
        inquilino.setNumeroTarjeta("4111111111111111");
        inquilino.setPaypalEmail("pepe@paypal.com");

        inmueble = new Inmueble();
    }

    @Test
    void constructorPorDefecto_dejaEstadoPendiente() {
        Reserva r = new Reserva();
        assertEquals("PENDIENTE", r.getEstado());
    }

    @Test
    void constructorCompleto_creaReservaConfirmadaYMetodoPago() {
        Date inicio = Date.valueOf("2025-01-10");
        Date fin = Date.valueOf("2025-01-12");

        Reserva r = new Reserva(inmueble, inquilino, inicio, fin, 200.0);

        assertEquals("CONFIRMADA", r.getEstado());
        assertEquals(200.0, r.getPrecioTotal());
        assertEquals("TARJETA", r.getMetodoPago());
    }

    @Test
    void confirmar_cambiaEstadoYMetodoPago() {
        Reserva r = new Reserva();
        r.setInquilino(inquilino);

        r.confirmar();

        assertEquals("CONFIRMADA", r.getEstado());
        assertEquals("TARJETA", r.getMetodoPago());
    }

    @Test
    void cancelar_cambiaEstadoACancelada() {
        Reserva r = new Reserva();
        r.cancelar("no importa motivo");

        assertEquals("CANCELADA", r.getEstado());
    }

    @Test
    void estaActiva_devuelveTrueSoloSiConfirmada() {
        Reserva r = new Reserva();
        assertFalse(r.estaActiva());

        r.setEstado("CONFIRMADA");
        assertTrue(r.estaActiva());

        r.setEstado("CANCELADA");
        assertFalse(r.estaActiva());
    }

    @Test
    void getNumeroNoches_calculaCorrectamente() {
        Reserva r = new Reserva();
        r.setFechaInicio(Date.valueOf("2025-01-10"));
        r.setFechaFin(Date.valueOf("2025-01-15"));

        assertEquals(5, r.getNumeroNoches());
    }

    @Test
    void getNumeroTarjeta_devuelveCorrecto() {
        Reserva r = new Reserva();
        r.setInquilino(inquilino);

        assertEquals("4111111111111111", r.getNumeroTarjeta());
    }

    @Test
    void getNumeroTarjeta_conInquilinoNullDevuelveNull() {
        Reserva r = new Reserva();
        assertNull(r.getNumeroTarjeta());
    }

    @Test
    void getPaypalEmail_devuelveCorrecto() {
        Reserva r = new Reserva();
        r.setInquilino(inquilino);

        assertEquals("pepe@paypal.com", r.getPaypalEmail());
    }

    @Test
    void getMetodoPago_priorizaMetodoPagoUsadoSobreInquilino() {
        Reserva r = new Reserva();
        r.setInquilino(inquilino);
        r.setMetodoPagoUsado("PAYPAL");

        assertEquals("PAYPAL", r.getMetodoPago());
    }

    @Test
    void getMetodoPago_siMetodoPagoUsadoNullUsaElDelInquilino() {
        Reserva r = new Reserva();
        r.setInquilino(inquilino);

        assertEquals("TARJETA", r.getMetodoPago());
    }

    @Test
    void settersYGettersFuncionan() {
        Reserva r = new Reserva();

        r.setId(10L);
        assertEquals(10L, r.getId());

        r.setPrecioTotal(199.99);
        assertEquals(199.99, r.getPrecioTotal());

        r.setEstado("COMPLETADA");
        assertEquals("COMPLETADA", r.getEstado());
    }
}

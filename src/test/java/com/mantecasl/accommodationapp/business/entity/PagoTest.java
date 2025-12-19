package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PagoTest {

    @Test
    void constructor_vacio_crea_objeto() {
        Pago pago = new Pago();

        assertNotNull(pago);
        assertNull(pago.getId());
        assertNull(pago.getReferencia());
    }

    @Test
    void getter_referencia_devuelve_valor() {
        Pago pago = new Pago();

        UUID ref = UUID.randomUUID();

        // usar reflexión para simular JPA
        try {
            var field = Pago.class.getDeclaredField("referencia");
            field.setAccessible(true);
            field.set(pago, ref);
        } catch (Exception e) {
            fail("Error usando reflexión");
        }

        assertEquals(ref, pago.getReferencia());
    }

    @Test
    void getter_id_devuelve_valor() {
        Pago pago = new Pago();

        try {
            var field = Pago.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(pago, 5L);
        } catch (Exception e) {
            fail("Error usando reflexión");
        }

        assertEquals(5L, pago.getId());
    }
}

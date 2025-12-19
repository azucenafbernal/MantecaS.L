package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GreetingTest {

    @Test
    void constructor_vacio_crea_objeto() {
        Greeting greeting = new Greeting();

        assertNotNull(greeting);
        assertNull(greeting.getId());
        assertNull(greeting.getMensaje());
    }

    @Test
    void constructor_con_mensaje_asigna_valor() {
        Greeting greeting = new Greeting("Hola mundo");

        assertEquals("Hola mundo", greeting.getMensaje());
    }

    @Test
    void setters_y_getters_funcionan() {
        Greeting greeting = new Greeting();

        greeting.setId(1L);
        greeting.setMensaje("Mensaje de prueba");

        assertEquals(1L, greeting.getId());
        assertEquals("Mensaje de prueba", greeting.getMensaje());
    }
}

package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PropietarioTest {

    @Test
    void constructor_y_getters_basicos_funcionan() {
        Usuario usuario = new Usuario("Juan", "juan@email.com", "1234");
        Propietario propietario = new Propietario(usuario, "600123123", "ES123");

        assertEquals(usuario, propietario.getUsuario());
        assertEquals("600123123", propietario.getTelefonoContacto());
        assertEquals("ES123", propietario.getCuentaBancaria());
    }

    @Test
    void agregarInmueble_asocia_correctamente_el_inmueble() {
        Propietario propietario = new Propietario();
        Inmueble inmueble = new Inmueble();

        propietario.agregarInmueble(inmueble);

        assertTrue(propietario.getInmuebles().contains(inmueble));
        assertEquals(propietario, inmueble.getPropietario());
    }

    @Test
    void getEmailUsuario_devuelve_email_cuando_existe_usuario() {
        Usuario usuario = new Usuario("Ana", "ana@email.com", "pass");
        Propietario propietario = new Propietario();
        propietario.setUsuario(usuario);

        assertEquals("ana@email.com", propietario.getEmailUsuario());
    }

    @Test
    void getEmailUsuario_devuelve_null_si_no_hay_usuario() {
        Propietario propietario = new Propietario();

        assertNull(propietario.getEmailUsuario());
    }

    @Test
    void getNombreUsuario_devuelve_nombre_cuando_existe_usuario() {
        Usuario usuario = new Usuario("Luis", "luis@email.com", "pass");
        Propietario propietario = new Propietario();
        propietario.setUsuario(usuario);

        assertEquals("Luis", propietario.getNombreUsuario());
    }

    @Test
    void getNombreUsuario_devuelve_null_si_no_hay_usuario() {
        Propietario propietario = new Propietario();

        assertNull(propietario.getNombreUsuario());
    }
}

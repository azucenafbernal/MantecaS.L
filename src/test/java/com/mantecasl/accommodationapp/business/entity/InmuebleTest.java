package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class InmuebleTest {

    @Test
    void constructorVacio_creaObjeto() {
        Inmueble i = new Inmueble();
        assertNotNull(i);
    }

    @Test
    void constructorCompleto_asignaValoresCorrectamente() {
        Propietario p = new Propietario();
        Inmueble i = new Inmueble("Calle Falsa", "123", "Madrid", "28001",
                50.0, "Bonito piso", 3, true, p);

        assertEquals("Calle Falsa", i.getCalle());
        assertEquals("123", i.getNumero());
        assertEquals("Madrid", i.getCiudad());
        assertEquals("28001", i.getCodigoPostal());
        assertEquals(50.0, i.getPrecioNoche());
        assertEquals("Bonito piso", i.getDescripcion());
        assertEquals(3, i.getCapacidad());
        assertEquals(p, i.getPropietario());
    }

    @Test
    void settersYGettersFuncionan() {
        Inmueble i = new Inmueble();

        i.setId(10L);
        i.setCalle("Sol");
        i.setNumero("8");
        i.setCiudad("Toledo");
        i.setCodigoPostal("45001");
        i.setPrecioNoche(99.99);
        i.setDescripcion("Casa rural");
        i.setCapacidad(5);
        i.setReservaDirecta(false);

        assertEquals(10L, i.getId());
        assertEquals("Sol", i.getCalle());
        assertEquals("8", i.getNumero());
        assertEquals("Toledo", i.getCiudad());
        assertEquals("45001", i.getCodigoPostal());
        assertEquals(99.99, i.getPrecioNoche());
        assertEquals("Casa rural", i.getDescripcion());
        assertEquals(5, i.getCapacidad());
        assertFalse(i.isReservaDirecta());
    }

    @Test
    void getDireccion_devuelveDireccionFormateada() {
        Inmueble i = new Inmueble();
        i.setCalle("Mayor");
        i.setNumero("10");
        i.setCiudad("Cuenca");
        i.setCodigoPostal("16001");

        assertEquals("Mayor 10, Cuenca 16001", i.getDireccion());
    }

    @Test
    void getUsuario_devuelveUsuarioDelPropietario() {
        Usuario u = new Usuario("Luis", "luis@mail.com", "1234");
        Propietario p = new Propietario();
        p.setUsuario(u);

        Inmueble i = new Inmueble();
        i.setPropietario(p);

        assertEquals(u, i.getUsuario());
    }

    @Test
    void getUsuario_devuelveNull_siNoHayPropietario() {
        Inmueble i = new Inmueble();
        assertNull(i.getUsuario());
    }

    @Test
    void listasInicialmenteVacias_peroModificables() {
        Inmueble i = new Inmueble();

        assertNotNull(i.getReservas());
        assertNotNull(i.getDisponibilidades());
        assertNotNull(i.getFavoritos());
        assertTrue(i.getReservas().isEmpty());
        assertTrue(i.getDisponibilidades().isEmpty());
        assertTrue(i.getFavoritos().isEmpty());

        // Probamos añadir elementos
        Reserva r = new Reserva();
        Disponibilidad d = new Disponibilidad();
        Favorito f = new Favorito();

        i.getReservas().add(r);
        i.getDisponibilidades().add(d);
        i.getFavoritos().add(f);

        assertEquals(1, i.getReservas().size());
        assertEquals(1, i.getDisponibilidades().size());
        assertEquals(1, i.getFavoritos().size());
    }
}

package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FavoritoTest {

    @Test
    void constructor_vacio_crea_objeto() {
        Favorito favorito = new Favorito();

        assertNotNull(favorito);
        assertNull(favorito.getUsuario());
        assertNull(favorito.getInmueble());
    }

    @Test
    void constructor_con_parametros_asigna_campos() {
        Usuario usuario = new Usuario();
        Inmueble inmueble = new Inmueble();

        Favorito favorito = new Favorito(usuario, inmueble);

        assertEquals(usuario, favorito.getUsuario());
        assertEquals(inmueble, favorito.getInmueble());
    }

    @Test
    void setters_y_getters_funcionan() {
        Favorito favorito = new Favorito();

        Usuario usuario = new Usuario();
        Inmueble inmueble = new Inmueble();

        favorito.setUsuario(usuario);
        favorito.setInmueble(inmueble);

        assertEquals(usuario, favorito.getUsuario());
        assertEquals(inmueble, favorito.getInmueble());
    }

    @Test
    void getId_por_defecto_es_null() {
        Favorito favorito = new Favorito();

        assertNull(favorito.getId());
    }
}

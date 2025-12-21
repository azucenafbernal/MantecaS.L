package com.mantecasl.accommodationapp.business.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void constructor_vacio_crea_usuario() {
        Usuario usuario = new Usuario();

        assertNotNull(usuario);
        assertNull(usuario.getId());
        assertNull(usuario.getNombre());
        assertNull(usuario.getEmail());
        assertNull(usuario.getContrasena());
    }

    @Test
    void constructor_con_parametros_asigna_campos() {
        Usuario usuario = new Usuario("Alejandro", "test@test.com", "1234");

        assertEquals("Alejandro", usuario.getNombre());
        assertEquals("test@test.com", usuario.getEmail());
        assertEquals("1234", usuario.getContrasena());
    }

    @Test
    void setters_y_getters_funcionan() {
        Usuario usuario = new Usuario();

        usuario.setId(10L);
        usuario.setNombre("Nombre");
        usuario.setEmail("email@email.com");
        usuario.setContrasena("pass");

        assertEquals(10L, usuario.getId());
        assertEquals("Nombre", usuario.getNombre());
        assertEquals("email@email.com", usuario.getEmail());
        assertEquals("pass", usuario.getContrasena());
    }

    @Test
    void setters_pueden_recibir_null() {
        Usuario usuario = new Usuario("A", "B", "C");

        usuario.setNombre(null);
        usuario.setEmail(null);
        usuario.setContrasena(null);

        assertNull(usuario.getNombre());
        assertNull(usuario.getEmail());
        assertNull(usuario.getContrasena());
    }
}

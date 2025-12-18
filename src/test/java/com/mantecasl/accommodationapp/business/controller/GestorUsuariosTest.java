package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GestorUsuarios.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class GestorUsuariosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioDAO usuarioDAO;

    // -------------------- GET --------------------

    @Test
    void mostrarFormulario_devuelve_vista_greeting_y_usuario_vacio() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("greeting"))
                .andExpect(model().attributeExists("usuario"));
    }

    // -------------------- POST --------------------

    @Test
    void registrarUsuario_email_ya_existe_devuelve_error() throws Exception {
        Usuario existente = new Usuario();
        existente.setEmail("test@test.com");

        when(usuarioDAO.findByEmail("test@test.com")).thenReturn(existente);

        mockMvc.perform(post("/usuarios")
                .param("nombre", "Alejandro")
                .param("email", "test@test.com")
                .param("contrasena", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("greeting"))
                .andExpect(model().attributeExists("error"));

        verify(usuarioDAO, never()).save(any());
    }

    @Test
    void registrarUsuario_email_nuevo_guarda_y_redirige_a_login() throws Exception {
        when(usuarioDAO.findByEmail("nuevo@test.com")).thenReturn(null);

        Usuario guardado = new Usuario();
        guardado.setId(1L);
        guardado.setEmail("nuevo@test.com");

        when(usuarioDAO.save(any(Usuario.class))).thenReturn(guardado);

        mockMvc.perform(post("/usuarios")
                .param("nombre", "Nuevo")
                .param("email", "nuevo@test.com")
                .param("contrasena", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("mensaje"));

        verify(usuarioDAO, times(1)).save(any(Usuario.class));
    }
}

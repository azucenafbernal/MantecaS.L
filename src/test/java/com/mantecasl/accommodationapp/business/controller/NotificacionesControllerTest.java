package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mantecasl.accommodationapp.business.entity.Usuario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificacionesController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class NotificacionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GestorNotificaciones gestorNotificaciones;

    @MockBean
    private GestorReservas gestorReservas;

    @Test
    void listarNotificaciones_redirige_si_no_hay_sesion() throws Exception {
        mockMvc.perform(get("/notificaciones"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void listarNotificaciones_devuelve_vista_si_hay_usuario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(gestorNotificaciones.obtenerNotificacionesUsuario(usuario))
                .thenReturn(java.util.List.of());

        when(gestorNotificaciones.contarNotificacionesNoLeidas(usuario))
                .thenReturn(0L);

        mockMvc.perform(get("/notificaciones").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("notificacionesUsuario"))
                .andExpect(model().attributeExists("notificaciones"))
                .andExpect(model().attributeExists("noLeidas"));
    }

    @Test
    void marcarComoLeida_redirige() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        mockMvc.perform(post("/notificaciones/1/leer").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notificaciones"));

        verify(gestorNotificaciones).marcarComoLeida(1L);
    }

    @Test
    void marcarTodasComoLeidas_redirige() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        mockMvc.perform(post("/notificaciones/leer-todas").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notificaciones"));

        verify(gestorNotificaciones).marcarTodasComoLeidas(usuario);
    }

    @Test
    void eliminarNotificacion_redirige() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        mockMvc.perform(post("/notificaciones/5/eliminar").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notificaciones"));

        verify(gestorNotificaciones).eliminarNotificacion(5L);
    }

    @Test
    void contarNoLeidas_devuelve_numero() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(gestorNotificaciones.contarNotificacionesNoLeidas(usuario))
                .thenReturn(3L);

        mockMvc.perform(get("/notificaciones/contar-no-leidas").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}

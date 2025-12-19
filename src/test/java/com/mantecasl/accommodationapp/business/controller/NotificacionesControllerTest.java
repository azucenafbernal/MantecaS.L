package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mantecasl.accommodationapp.business.entity.Usuario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(controllers = NotificacionesController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@Import(NotificacionesControllerTest.TestViewResolverConfig.class)
class NotificacionesControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private GestorNotificaciones gestorNotificaciones;

        @MockBean
        private GestorReservas gestorReservas;

        // ---------- ViewResolver dummy ----------
        @TestConfiguration
        static class TestViewResolverConfig {
                @Bean
                ViewResolver viewResolver() {
                        return (String viewName, Locale locale) -> new AbstractView() {
                                @Override
                                protected void renderMergedOutputModel(
                                                Map<String, Object> model,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
                                }
                        };
                }
        }

        // ---------- LISTAR ----------
        @Test
        void listarNotificaciones_sin_usuario() throws Exception {
                mockMvc.perform(get("/notificaciones"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("redirect:/login"));
        }

        @Test
        void listarNotificaciones_con_usuario() throws Exception {
                Usuario u = new Usuario();
                u.setId(1L);

                MockHttpSession session = new MockHttpSession();
                session.setAttribute("usuario", u);

                when(gestorNotificaciones.obtenerNotificacionesUsuario(u))
                                .thenReturn(List.of());

                when(gestorNotificaciones.contarNotificacionesNoLeidas(u))
                                .thenReturn(0L);

                mockMvc.perform(get("/notificaciones").session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("notificacionesUsuario"));
        }

        // ---------- PROPIETARIO ----------
        @Test
        void verNotificacionesPropietario_sin_usuario() throws Exception {
                mockMvc.perform(get("/notificaciones/propietario"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("redirect:/login"));
        }

        @Test
        void verNotificacionesPropietario_con_usuario() throws Exception {
                Usuario u = new Usuario();
                u.setId(1L);

                MockHttpSession session = new MockHttpSession();
                session.setAttribute("usuario", u);

                when(gestorReservas.obtenerSolicitudesPendientesPropietario(1L))
                                .thenReturn(List.of());
                when(gestorReservas.obtenerSolicitudesAprobadasPropietario(1L))
                                .thenReturn(List.of());
                when(gestorReservas.obtenerSolicitudesRechazadasPropietario(1L))
                                .thenReturn(List.of());
                when(gestorReservas.obtenerHistorialReservasPropietario(1L))
                                .thenReturn(List.of());

                mockMvc.perform(get("/notificaciones/propietario").session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("notificaciones"));
        }

        // ---------- LEER ----------
        @Test
        void marcarComoLeida_con_usuario() throws Exception {
                Usuario u = new Usuario();
                u.setId(1L);

                MockHttpSession session = new MockHttpSession();
                session.setAttribute("usuario", u);

                mockMvc.perform(post("/notificaciones/1/leer").session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("redirect:/notificaciones"));

                verify(gestorNotificaciones).marcarComoLeida(1L);
        }

        @Test
        void marcarComoLeida_sin_usuario() throws Exception {
                mockMvc.perform(post("/notificaciones/1/leer"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("redirect:/notificaciones"));

                verify(gestorNotificaciones, never()).marcarComoLeida(any());
        }

        // ---------- LEER TODAS ----------
        @Test
        void marcarTodasComoLeidas_sin_usuario() throws Exception {
                mockMvc.perform(post("/notificaciones/leer-todas"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("redirect:/notificaciones"));

                verify(gestorNotificaciones, never()).marcarTodasComoLeidas(any());
        }

        // ---------- ELIMINAR ----------
        @Test
        void eliminarNotificacion_sin_usuario() throws Exception {
                mockMvc.perform(post("/notificaciones/5/eliminar"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("redirect:/notificaciones"));

                verify(gestorNotificaciones, never()).eliminarNotificacion(any());
        }

        // ---------- CONTAR ----------
        @Test
        void contarNoLeidas_con_usuario() throws Exception {
                Usuario u = new Usuario();
                u.setId(1L);

                MockHttpSession session = new MockHttpSession();
                session.setAttribute("usuario", u);

                when(gestorNotificaciones.contarNotificacionesNoLeidas(u))
                                .thenReturn(3L);

                mockMvc.perform(get("/notificaciones/contar-no-leidas").session(session))
                                .andExpect(status().isOk())
                                .andExpect(content().string("3"));
        }

        @Test
        void contarNoLeidas_sin_usuario() throws Exception {
                mockMvc.perform(get("/notificaciones/contar-no-leidas"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("0"));
        }
}

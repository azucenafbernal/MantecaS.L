package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

@WebMvcTest(controllers = IndexController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SolicitudReservaDAO solicitudReservaDAO;

    @MockBean
    private PropietarioDAO propietarioDAO;

    @MockBean
    private InmuebleDAO inmuebleDAO;

    @MockBean
    private GestorNotificaciones gestorNotificaciones;

    // 👇 ViewResolver falso para evitar el error circular
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
                    // No hace nada
                }
            };
        }
    }

    @Test
    void mostrarIndex_sinUsuario() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void mostrarIndex_usuarioNoPropietario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(null);
        when(gestorNotificaciones.contarNotificacionesNoLeidas(usuario)).thenReturn(0L);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("esPropietario", false))
                .andExpect(model().attribute("numeroNotificacionesPendientes", 0))
                .andExpect(model().attribute("notificacionesNoLeidas", 0L));
    }

    @Test
    void mostrarIndex_usuarioPropietario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Propietario propietario = new Propietario();
        propietario.setId(10L);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(100L);
        inmueble.setPropietario(propietario);

        SolicitudReserva solicitud = new SolicitudReserva();
        solicitud.setEstado("PENDIENTE");
        solicitud.setInmueble(inmueble);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(propietario);
        when(inmuebleDAO.findAll()).thenReturn(List.of(inmueble));
        when(solicitudReservaDAO.findAll()).thenReturn(List.of(solicitud));
        when(gestorNotificaciones.contarNotificacionesNoLeidas(usuario)).thenReturn(2L);

        mockMvc.perform(get("/index").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("esPropietario", true))
                .andExpect(model().attribute("numeroNotificacionesPendientes", 1L))
                .andExpect(model().attribute("notificacionesNoLeidas", 2L));
    }

    @Test
    void mostrarIndex_propietario_con_ramas_falsas() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(5L);

        Propietario propietario = new Propietario();
        propietario.setId(50L);

        // Inmueble SIN propietario (rama false)
        Inmueble inmuebleSinPropietario = new Inmueble();
        inmuebleSinPropietario.setId(1L);

        // Inmueble de OTRO propietario (equals false)
        Propietario otroPropietario = new Propietario();
        otroPropietario.setId(99L);

        Inmueble inmuebleOtroPropietario = new Inmueble();
        inmuebleOtroPropietario.setId(2L);
        inmuebleOtroPropietario.setPropietario(otroPropietario);

        // Solicitud NO pendiente (rama false)
        SolicitudReserva solicitudNoPendiente = new SolicitudReserva();
        solicitudNoPendiente.setEstado("APROBADA");
        solicitudNoPendiente.setInmueble(inmuebleOtroPropietario);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(propietarioDAO.findByUsuarioId(5L)).thenReturn(propietario);
        when(inmuebleDAO.findAll()).thenReturn(List.of(
                inmuebleSinPropietario,
                inmuebleOtroPropietario));
        when(solicitudReservaDAO.findAll()).thenReturn(List.of(solicitudNoPendiente));
        when(gestorNotificaciones.contarNotificacionesNoLeidas(usuario)).thenReturn(0L);

        mockMvc.perform(get("/index").session(session))
                .andExpect(status().isOk());
    }

}

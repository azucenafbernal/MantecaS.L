package com.mantecasl.accommodationapp.business.controller;

import org.springframework.lang.NonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

@WebMvcTest(controllers = SolicitudesController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@Import(SolicitudesControllerTest.TestViewResolverConfig.class)
class SolicitudesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitudReservaDAO solicitudReservaDAO;

    @MockitoBean
    private ReservaDAO reservaDAO;

    @MockitoBean
    private DisponibilidadDAO disponibilidadDAO;

    @MockitoBean
    private GestorDisponibilidad gestorDisponibilidad;

    @MockitoBean
    private PropietarioDAO propietarioDAO;

    @MockitoBean
    private GestorNotificaciones notificacion;

    @MockitoBean
    private InmuebleDAO inmuebleDAO;

    // ---------- ViewResolver dummy ----------
    static class TestViewResolverConfig {
        @Bean
        ViewResolver viewResolver() {
            return (String viewName, Locale locale) -> new AbstractView() {
                @Override
                protected void renderMergedOutputModel(
                        @NonNull Map<String, Object> model,
                        @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response) {
                }
            };
        }
    }

    // ---------- VER SOLICITUD ----------

    @Test
    void verSolicitud_usuario_no_logueado() throws Exception {
        mockMvc.perform(get("/propietario/reserva/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void verSolicitud_ok() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Propietario propietario = new Propietario();
        propietario.setId(10L);

        Inmueble inmueble = new Inmueble();
        inmueble.setPropietario(propietario);

        SolicitudReserva solicitud = new SolicitudReserva();
        solicitud.setInmueble(inmueble);

        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(propietario);
        when(solicitudReservaDAO.findById(5L)).thenReturn(Optional.of(solicitud));

        mockMvc.perform(get("/propietario/reserva/5")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("peticion"))
                .andExpect(model().attributeExists("reserva"));
    }

    @Test
    void verSolicitud_no_es_propietario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Propietario propietario = new Propietario();
        propietario.setId(10L);

        Propietario otro = new Propietario();
        otro.setId(20L);

        Inmueble inmueble = new Inmueble();
        inmueble.setPropietario(otro);

        SolicitudReserva solicitud = new SolicitudReserva();
        solicitud.setInmueble(inmueble);

        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(propietario);
        when(solicitudReservaDAO.findById(1L)).thenReturn(Optional.of(solicitud));

        mockMvc.perform(get("/propietario/reserva/1")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/"));
    }

    // ---------- APROBAR ----------

    @Test
    void aprobarSolicitud_ok() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Propietario propietario = new Propietario();
        propietario.setId(10L);

        Inquilino inquilino = new Inquilino();
        inquilino.setMetodoPago("TARJETA");

        Inmueble inmueble = new Inmueble();
        inmueble.setPropietario(propietario);

        SolicitudReserva solicitud = new SolicitudReserva();
        solicitud.setInmueble(inmueble);
        solicitud.setInquilino(inquilino);

        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(propietario);
        when(solicitudReservaDAO.findById(1L)).thenReturn(Optional.of(solicitud));
        when(gestorDisponibilidad.verificarDisponibilidad(any(), any(), any()))
                .thenReturn(true);

        mockMvc.perform(post("/propietario/reserva/1/aprobar")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/propietario/notificaciones?exito=Solicitud+aprobada"));

        verify(solicitudReservaDAO).save(any());
        verify(notificacion).crearNotificacionSolicitudAprobada(any());
    }

    // ---------- RECHAZAR ----------

    @Test
    void rechazarSolicitud_ok() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Propietario propietario = new Propietario();
        propietario.setId(10L);

        Inmueble inmueble = new Inmueble();
        inmueble.setPropietario(propietario);

        SolicitudReserva solicitud = new SolicitudReserva();
        solicitud.setInmueble(inmueble);

        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(propietario);
        when(solicitudReservaDAO.findById(1L)).thenReturn(Optional.of(solicitud));

        mockMvc.perform(post("/propietario/reserva/1/rechazar")
                .param("motivo", "No disponible")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/propietario/notificaciones?exito=Solicitud+rechazada"));

        verify(solicitudReservaDAO).save(any());
        verify(notificacion).crearNotificacionSolicitudRechazada(any(), any());
    }
}

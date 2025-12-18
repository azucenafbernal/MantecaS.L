package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(controllers = ReservasController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class ReservasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GestorDisponibilidad gestorDisponibilidad;

    @MockBean
    private InmuebleDAO inmuebleDAO;

    @MockBean
    private ReservaDAO reservaDAO;

    @MockBean
    private InquilinoDAO inquilinoDAO;

    @MockBean
    private SolicitudReservaDAO solicitudReservaDAO;

    @MockBean
    private DisponibilidadDAO disponibilidadDAO;

    @MockBean
    private GestorNotificaciones notificacion;

    // -------- ViewResolver dummy --------
    @TestConfiguration
    static class DummyViewResolverConfig {
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

    // -------- GET /reservas/nueva/{id} --------

    @Test
    void mostrarFormulario_usuario_no_logueado() throws Exception {
        mockMvc.perform(get("/reservas/nueva/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/login?from=/reservas/nueva/1"));
    }

    @Test
    void mostrarFormulario_inmueble_no_existe() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservas/nueva/1")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void mostrarFormulario_ok() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(1L);
        inmueble.setReservaDirecta(true);

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));

        mockMvc.perform(get("/reservas/nueva/1")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva-inmueble"))
                .andExpect(model().attributeExists("inmueble"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("esReservaDirecta"));
    }

    // -------- POST /reservas/confirmar --------

    @Test
    void confirmarReserva_error_en_fechas() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(gestorDisponibilidad.validarFechas(any(), any()))
                .thenThrow(new RuntimeException("Error fechas"));

        Inmueble inmueble = new Inmueble();
        inmueble.setId(1L);
        inmueble.setReservaDirecta(true);

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));

        mockMvc.perform(post("/reservas/confirmar")
                .sessionAttr("usuario", usuario)
                .param("inmuebleId", "1")
                .param("fechaInicio", "2025-01-10")
                .param("fechaFin", "2025-01-05")
                .param("telefono", "600000000")
                .param("documentoIdentidad", "12345678A")
                .param("metodoPago", "TARJETA"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva-inmueble"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void confirmarReserva_reserva_directa_ok() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(1L);
        inmueble.setReservaDirecta(true);
        inmueble.setPrecioNoche(50.0);

        Propietario propietario = new Propietario();
        propietario.setUsuario(new Usuario());
        inmueble.setPropietario(propietario);

        Inquilino inquilino = new Inquilino();
        inquilino.setUsuario(usuario);

        when(gestorDisponibilidad.validarFechas(any(), any()))
                .thenReturn(new Date[] {
                        Date.valueOf(LocalDate.now().plusDays(1)),
                        Date.valueOf(LocalDate.now().plusDays(3))
                });

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));
        when(gestorDisponibilidad.verificarDisponibilidad(any(), any(), any()))
                .thenReturn(true);
        when(inquilinoDAO.findByUsuario(usuario))
                .thenReturn(Optional.of(inquilino));

        mockMvc.perform(post("/reservas/confirmar")
                .sessionAttr("usuario", usuario)
                .param("inmuebleId", "1")
                .param("fechaInicio", LocalDate.now().plusDays(1).toString())
                .param("fechaFin", LocalDate.now().plusDays(3).toString())
                .param("telefono", "600000000")
                .param("documentoIdentidad", "12345678A")
                .param("metodoPago", "TARJETA")
                .param("numeroTarjeta", "4111111111111111")
                .param("fechaCaducidad", "12/30")
                .param("cvv", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmacion-reserva"))
                .andExpect(model().attributeExists("reserva"))
                .andExpect(model().attributeExists("mensaje"));

        verify(notificacion).crearNotificacionReservaDirecta(any());
    }
}

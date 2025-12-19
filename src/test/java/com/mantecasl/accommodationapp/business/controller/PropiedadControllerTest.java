package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(controllers = PropiedadController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@Import(PropiedadControllerTest.TestViewResolverConfig.class)
class PropiedadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InmuebleDAO inmuebleDAO;

    @MockBean
    private GestorDisponibilidad gestorDisponibilidad;

    @MockBean
    private ReservaDAO reservaDAO;

    @MockBean
    private GestorNotificaciones notificacion;

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

    // ---------- CATALOGO ----------

    @Test
    void verCatalogo_filtra_propiedades_sin_propietario() throws Exception {
        Inmueble invalido = new Inmueble(); // sin propietario
        Inmueble valido = new Inmueble();
        valido.setId(1L);
        valido.setPropietario(new Propietario());

        when(inmuebleDAO.findAll()).thenReturn(List.of(invalido, valido));

        mockMvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"))
                .andExpect(model().attributeExists("propiedades"));
    }

    @Test
    void verCatalogo_formato_fecha_invalido() throws Exception {
        when(inmuebleDAO.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/catalogo")
                .param("fechaInicio", "no-fecha")
                .param("fechaFin", "otra"))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void verCatalogo_disponibilidad_false() throws Exception {
        Inmueble i = new Inmueble();
        i.setId(1L);
        i.setPropietario(new Propietario());

        when(inmuebleDAO.findAll()).thenReturn(List.of(i));
        when(gestorDisponibilidad.verificarDisponibilidad(any(), any(), any()))
                .thenReturn(false);

        mockMvc.perform(get("/catalogo")
                .param("fechaInicio", LocalDate.now().plusDays(1).toString())
                .param("fechaFin", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"))
                .andExpect(model().attributeExists("mensaje"));
    }

    @Test
    void verCatalogo_solo_capacidad() throws Exception {
        Inmueble i = new Inmueble();
        i.setCapacidad(4);
        i.setPropietario(new Propietario());

        when(inmuebleDAO.findAll()).thenReturn(List.of(i));

        mockMvc.perform(get("/catalogo")
                .param("capacidad", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"));
    }

    // ---------- ELIMINAR PROPIEDAD ----------

    @Test
    void eliminarPropiedad_ok_happy_path() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        Usuario up = new Usuario();
        up.setId(1L);

        Propietario p = new Propietario();
        p.setUsuario(up);

        Inmueble i = new Inmueble();
        i.setId(10L);
        i.setPropietario(p);

        Reserva r = new Reserva();
        r.setPrecioTotal(100);

        when(inmuebleDAO.findById(10L)).thenReturn(Optional.of(i));
        when(reservaDAO.findReservasFuturasByInmueble(10L))
                .thenReturn(List.of(r));

        mockMvc.perform(post("/propiedades/10/eliminar")
                .sessionAttr("usuario", u))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/propiedades"));

        verify(notificacion).notificarEliminacionPropiedad(eq(i), any());
        verify(notificacion).crearNotificacionPagoDevuelto(eq(r), eq(100.0));
        verify(inmuebleDAO).delete(i);
    }

    @Test
    void eliminarPropiedad_excepcion_general() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        when(inmuebleDAO.findById(5L))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/propiedades/5/eliminar")
                .sessionAttr("usuario", u))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/propiedades/5"));
    }
}

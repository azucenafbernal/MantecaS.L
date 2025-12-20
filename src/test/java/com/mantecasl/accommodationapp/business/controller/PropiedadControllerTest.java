package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

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

        // ================= VIEW RESOLVER =================

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
                                        // This method is intentionally left empty because
                                        // the test context does not require actual view rendering.
                                }
                        };
                }
        }

        // ================== CATALOGO ==================

        @Test
        void verCatalogo_sin_filtros() throws Exception {
                Inmueble i = new Inmueble();
                i.setPropietario(new Propietario());

                when(inmuebleDAO.findAll()).thenReturn(List.of(i));

                mockMvc.perform(get("/catalogo"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("lista-propiedades"))
                                .andExpect(model().attributeExists("propiedades"));
        }

        @Test
        void verCatalogo_filtra_por_ciudad() throws Exception {
                Inmueble i = new Inmueble();
                i.setCiudad("Madrid");
                i.setPropietario(new Propietario());

                when(inmuebleDAO.findAll()).thenReturn(List.of(i));

                mockMvc.perform(get("/catalogo")
                                .param("ciudad", "mad"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("lista-propiedades"));
        }

        @Test
        void verCatalogo_filtra_por_capacidad() throws Exception {
                Inmueble i = new Inmueble();
                i.setCapacidad(4);
                i.setPropietario(new Propietario());

                when(inmuebleDAO.findAll()).thenReturn(List.of(i));

                mockMvc.perform(get("/catalogo")
                                .param("capacidad", "2"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("lista-propiedades"));
        }

        @Test
        void verCatalogo_con_fechas_disponible_false() throws Exception {
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
                                .andExpect(model().attributeExists("mensaje"));
        }

        @Test
        void verCatalogo_fecha_inicio_pasada() throws Exception {
                when(inmuebleDAO.findAll()).thenReturn(List.of());

                mockMvc.perform(get("/catalogo")
                                .param("fechaInicio", LocalDate.now().minusDays(1).toString())
                                .param("fechaFin", LocalDate.now().plusDays(2).toString()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("error-catalogo"))
                                .andExpect(model().attributeExists("error"));
        }

        @Test
        void verCatalogo_fecha_fin_invalida() throws Exception {
                when(inmuebleDAO.findAll()).thenReturn(List.of());

                mockMvc.perform(get("/catalogo")
                                .param("fechaInicio", LocalDate.now().plusDays(2).toString())
                                .param("fechaFin", LocalDate.now().plusDays(1).toString()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("error-catalogo"))
                                .andExpect(model().attributeExists("error"));
        }

        @Test
        void verCatalogo_sin_resultados_muestra_mensaje() throws Exception {
                when(inmuebleDAO.findAll()).thenReturn(List.of());

                mockMvc.perform(get("/catalogo"))
                                .andExpect(status().isOk())
                                .andExpect(model().attributeExists("mensaje"));
        }

        // ================== ELIMINAR ==================

        @Test
        void eliminarPropiedad_ok() throws Exception {
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
                verify(notificacion).crearNotificacionPagoDevuelto(r, 100.0);
                verify(inmuebleDAO).delete(i);
        }

        @Test
        void eliminarPropiedad_no_es_propietario() throws Exception {
                Usuario u = new Usuario();
                u.setId(1L);

                Usuario up = new Usuario();
                up.setId(2L);

                Propietario p = new Propietario();
                p.setUsuario(up);

                Inmueble i = new Inmueble();
                i.setId(5L);
                i.setPropietario(p);

                when(inmuebleDAO.findById(5L)).thenReturn(Optional.of(i));

                mockMvc.perform(post("/propiedades/5/eliminar")
                                .sessionAttr("usuario", u))
                                .andExpect(status().isOk())
                                .andExpect(view().name("error-eliminar"));

                verify(inmuebleDAO, never()).delete(any());
        }

        @Test
        void eliminarPropiedad_excepcion() throws Exception {
                Usuario u = new Usuario();
                u.setId(1L);

                when(inmuebleDAO.findById(99L))
                                .thenThrow(new RuntimeException("boom"));

                mockMvc.perform(post("/propiedades/99/eliminar")
                                .sessionAttr("usuario", u))
                                .andExpect(status().isOk())
                                .andExpect(view().name("error-eliminar"));
        }
}

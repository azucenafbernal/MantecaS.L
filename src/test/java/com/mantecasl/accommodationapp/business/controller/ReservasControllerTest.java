package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Date;
import java.time.LocalDate;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

@WebMvcTest(controllers = ReservasController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@Import(ReservasControllerTest.TestViewResolverConfig.class)
class ReservasControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private GestorDisponibilidad gestorDisponibilidad;

        @MockitoBean
        private InmuebleDAO inmuebleDAO;

        @MockitoBean
        private ReservaDAO reservaDAO;

        @MockitoBean
        private InquilinoDAO inquilinoDAO;

        @MockitoBean
        private SolicitudReservaDAO solicitudReservaDAO;

        @MockitoBean
        private DisponibilidadDAO disponibilidadDAO;

        @MockitoBean
        private GestorNotificaciones notificacion;

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

        // ---------- GET /reservas/nueva/{id} ----------

        @Test
        void mostrarFormulario_no_logueado() throws Exception {
                mockMvc.perform(get("/reservas/nueva/1"))
                                .andExpect(status().isOk());
        }

        @Test
        void mostrarFormulario_inmueble_no_existe() throws Exception {
                when(inmuebleDAO.findById(1L)).thenReturn(Optional.empty());

                mockMvc.perform(get("/reservas/nueva/1")
                                .sessionAttr("usuario", new Usuario()))
                                .andExpect(status().isOk());
        }

        // ---------- POST /reservas/confirmar ----------

        @Test
        void confirmarReserva_error_validar_fechas() throws Exception {
                Usuario usuario = new Usuario();
                usuario.setId(1L);

                when(gestorDisponibilidad.validarFechas(anyString(), anyString()))
                                .thenThrow(new RuntimeException("Fechas inválidas"));

                mockMvc.perform(post("/reservas/confirmar")
                                .sessionAttr("usuario", usuario)
                                .param("inmuebleId", "1")
                                .param("fechaInicio", "2025-01-10")
                                .param("fechaFin", "2025-01-09")
                                .param("telefono", "600")
                                .param("documentoIdentidad", "DNI")
                                .param("metodoPago", "TARJETA"))
                                .andExpect(status().isOk());
        }

        @Test
        void confirmarReserva_directa_flujo_basico() throws Exception {
                Usuario usuario = new Usuario();
                usuario.setId(1L);

                Inmueble inmueble = new Inmueble();
                inmueble.setId(1L);
                inmueble.setPrecioNoche(100);
                inmueble.setReservaDirecta(true);

                when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));

                when(gestorDisponibilidad.validarFechas(anyString(), anyString()))
                                .thenReturn(new Date[] {
                                                Date.valueOf(LocalDate.now().plusDays(1)),
                                                Date.valueOf(LocalDate.now().plusDays(3))
                                });

                when(gestorDisponibilidad.verificarDisponibilidad(
                                anyLong(),
                                any(Date.class),
                                any(Date.class)))
                                .thenReturn(true);

                when(inquilinoDAO.findByUsuario(usuario)).thenReturn(Optional.empty());
                when(inquilinoDAO.save(isA(Inquilino.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                mockMvc.perform(post("/reservas/confirmar")
                                .sessionAttr("usuario", usuario)
                                .param("inmuebleId", "1")
                                .param("fechaInicio", "2025-01-10")
                                .param("fechaFin", "2025-01-12")
                                .param("telefono", "600")
                                .param("documentoIdentidad", "DNI")
                                .param("metodoPago", "TARJETA"))
                                .andExpect(status().isOk());

                verify(inquilinoDAO, atLeastOnce()).save(isA(Inquilino.class));

        }
}

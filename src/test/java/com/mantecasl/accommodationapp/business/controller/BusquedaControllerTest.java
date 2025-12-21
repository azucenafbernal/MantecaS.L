package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BusquedaController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class BusquedaControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private GestorDisponibilidad gestorDisponibilidad;

        @MockitoBean
        private InmuebleDAO inmuebleDAO;

        @Test
        void buscarInmuebles_devuelve_lista_propiedades() throws Exception {
                Date entrada = Date.valueOf("2025-01-10");
                Date salida = Date.valueOf("2025-01-15");

                when(gestorDisponibilidad.validarFechas(any(), any()))
                                .thenReturn(new Date[] { entrada, salida });

                when(gestorDisponibilidad.buscarInmueblesDisponibles(
                                any(), any(), any(), any()))
                                .thenReturn(List.of(new Inmueble()));

                mockMvc.perform(get("/buscar")
                                .param("fechaInicio", "2025-01-10")
                                .param("fechaFin", "2025-01-15")
                                .param("ciudad", "Madrid")
                                .param("capacidad", "2"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("lista-propiedades"));
        }

        @Test
        void verInmueble_devuelve_detalle_si_existe() throws Exception {
                Inmueble inmueble = new Inmueble();
                when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));

                mockMvc.perform(get("/inmueble/1"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("detalle-inmueble"))
                                .andExpect(model().attributeExists("inmueble"));
        }

        @Test
        void verInmueble_redirige_si_no_existe() throws Exception {
                when(inmuebleDAO.findById(99L)).thenReturn(Optional.empty());

                mockMvc.perform(get("/inmueble/99"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/"));
        }
}

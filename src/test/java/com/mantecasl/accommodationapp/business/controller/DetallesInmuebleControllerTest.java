package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import org.springframework.lang.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DetallesInmuebleController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class DetallesInmuebleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InmuebleDAO inmuebleDAO;

    // ---------- ViewResolver dummy ----------
    @TestConfiguration
    static class TestViewResolverConfig {
        @Bean
        ViewResolver viewResolver() {
            return (viewName, locale) -> new AbstractView() {
                @Override
                protected void renderMergedOutputModel(

                        @NonNull Map<String, Object> model,
                        @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response) {
                    // no-op
                }
            };
        }
    }

    // ---------- CLASE DE EQUIVALENCIA: inmueble NO existe ----------
    @Test
    void verDetalles_inmuebleNoExiste() throws Exception {
        when(inmuebleDAO.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/gestor/propiedad/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/lista-propiedades"));

        verify(inmuebleDAO).findById(1L);
        verifyNoMoreInteractions(inmuebleDAO);
    }

    // ---------- CLASE DE EQUIVALENCIA: inmueble EXISTE ----------
    @Test
    void verDetalles_inmuebleExiste() throws Exception {
        Inmueble inmueble = new Inmueble();
        inmueble.setId(1L);

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));

        mockMvc.perform(get("/gestor/propiedad/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-inmueble"))
                .andExpect(model().attribute("inmueble", inmueble));

        verify(inmuebleDAO).findById(1L);
        verifyNoMoreInteractions(inmuebleDAO);
    }
}

package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DetallesInmuebleController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@Import(DetallesInmuebleControllerTest.TestViewResolverConfig.class)
class DetallesInmuebleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InmuebleDAO inmuebleDAO;

    // ---------- ViewResolver dummy ----------
    @TestConfiguration
    static class TestViewResolverConfig {
        @Bean
        ViewResolver viewResolver() {
            return (viewName, locale) -> new AbstractView() {
                @Override
                protected void renderMergedOutputModel(
                        Map<String, Object> model,
                        HttpServletRequest request,
                        HttpServletResponse response) {
                    // no-op
                }
            };
        }
    }

    // ---------- TEST 1: inmueble NO existe → redirect ----------
    @Test
    void verDetalles_inmuebleNoExiste() throws Exception {
        when(inmuebleDAO.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/gestor/propiedad/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/lista-propiedades"));
    }

    // ---------- TEST 2: inmueble EXISTE → view detalle ----------
    @Test
    void verDetalles_inmuebleExiste() throws Exception {
        Inmueble inmueble = new Inmueble();
        inmueble.setId(1L);

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));

        mockMvc.perform(get("/gestor/propiedad/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-inmueble"))
                .andExpect(model().attributeExists("inmueble"));
    }
}

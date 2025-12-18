package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Optional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

@WebMvcTest(controllers = PropiedadController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
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

    // ViewResolver dummy (misma copla de siempre)
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

    // ---------- GET /catalogo ----------

    @Test
    void verCatalogo_sin_filtros_devuelve_lista() throws Exception {
        Inmueble inmueble = new Inmueble();
        inmueble.setId(1L);
        inmueble.setPropietario(new Propietario());

        when(inmuebleDAO.findAll()).thenReturn(List.of(inmueble));

        mockMvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"))
                .andExpect(model().attributeExists("propiedades"));
    }

    @Test
    void verCatalogo_fecha_inicio_en_pasado_devuelve_error() throws Exception {
        when(inmuebleDAO.findAll()).thenReturn(List.of());

        String ayer = LocalDate.now().minusDays(1).toString();

        mockMvc.perform(get("/catalogo")
                .param("fechaInicio", ayer)
                .param("fechaFin", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void verCatalogo_fecha_fin_antes_inicio_devuelve_error() throws Exception {
        when(inmuebleDAO.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/catalogo")
                .param("fechaInicio", "2025-01-10")
                .param("fechaFin", "2025-01-09"))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void verCatalogo_filtro_ciudad_y_capacidad() throws Exception {
        Inmueble inmueble = new Inmueble();
        inmueble.setId(1L);
        inmueble.setCiudad("Madrid");
        inmueble.setCapacidad(4);
        inmueble.setPropietario(new Propietario());

        when(inmuebleDAO.findAll()).thenReturn(List.of(inmueble));
        when(gestorDisponibilidad.verificarDisponibilidad(any(), any(), any()))
                .thenReturn(true);

        mockMvc.perform(get("/catalogo")
                .param("ciudad", "madrid")
                .param("capacidad", "2")
                .param("fechaInicio", LocalDate.now().plusDays(1).toString())
                .param("fechaFin", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"))
                .andExpect(model().attributeExists("propiedades"));
    }

    // ---------- POST eliminar ----------

    @Test
    void eliminarPropiedad_usuario_no_propietario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Usuario otro = new Usuario();
        otro.setId(2L);

        Propietario propietario = new Propietario();
        propietario.setUsuario(otro);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(10L);
        inmueble.setPropietario(propietario);

        when(inmuebleDAO.findById(10L)).thenReturn(Optional.of(inmueble));

        mockMvc.perform(post("/propiedades/10/eliminar")
                .sessionAttr("usuario", usuario))
                .andExpect(view().name(org.hamcrest.Matchers.startsWith("redirect:")));
    }

}

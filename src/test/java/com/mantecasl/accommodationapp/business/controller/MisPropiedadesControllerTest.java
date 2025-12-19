package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.Map;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

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

@WebMvcTest(controllers = MisPropiedadesController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@Import(MisPropiedadesControllerTest.TestViewResolverConfig.class)
class MisPropiedadesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InmuebleDAO inmuebleDAO;

    @MockBean
    private ReservaDAO reservaDAO;

    @MockBean
    private FavoritoDAO favoritoDAO;

    @MockBean
    private PagoDAO pagoDAO;

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

    // ---------- MOSTRAR ----------
    @Test
    void mostrarMisPropiedades_no_logueado() throws Exception {
        mockMvc.perform(get("/mis-propiedades"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void mostrarMisPropiedades_logueado() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        when(inmuebleDAO.findByPropietarioUsuarioId(1L))
                .thenReturn(List.of(new Inmueble()));

        mockMvc.perform(get("/mis-propiedades")
                .sessionAttr("usuario", u))
                .andExpect(status().isOk())
                .andExpect(view().name("modificar-propiedad"));
    }

    // ---------- ELIMINAR ----------
    @Test
    void eliminarPropiedad_no_logueado() throws Exception {
        mockMvc.perform(get("/eliminar-propiedad/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void eliminarPropiedad_excepcion_oculta() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        Usuario up = new Usuario();
        up.setId(1L);

        Propietario p = new Propietario();
        p.setUsuario(up);

        Inmueble i = new Inmueble();
        i.setId(1L);
        i.setPropietario(p);

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(i));
        doThrow(new RuntimeException()).when(reservaDAO).deleteByInmuebleId(1L);
        when(inmuebleDAO.save(any())).thenReturn(i);

        mockMvc.perform(get("/eliminar-propiedad/1")
                .sessionAttr("usuario", u))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/mis-propiedades"));

        verify(inmuebleDAO).save(i);
    }

    // ---------- EDITAR ----------
    @Test
    void editarPropiedad_ok() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        Propietario p = new Propietario();
        Usuario up = new Usuario();
        up.setId(1L);
        p.setUsuario(up);

        Inmueble i = new Inmueble();
        i.setId(5L);
        i.setPropietario(p);

        when(inmuebleDAO.findById(5L)).thenReturn(Optional.of(i));

        mockMvc.perform(get("/editar-propiedad/5")
                .sessionAttr("usuario", u))
                .andExpect(status().isOk())
                .andExpect(view().name("editar-propiedad"))
                .andExpect(model().attributeExists("inmueble"));
    }

    // ---------- ACTUALIZAR ----------
    @Test
    void actualizarPropiedad_no_logueado() throws Exception {
        mockMvc.perform(post("/actualizar-propiedad")
                .param("id", "1")
                .param("calle", "C")
                .param("numero", "1")
                .param("ciudad", "M")
                .param("codigoPostal", "28000")
                .param("precioNoche", "50")
                .param("capacidad", "2")
                .param("descripcion", "D"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void actualizarPropiedad_es_propietario() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        Usuario up = new Usuario();
        up.setId(1L);

        Propietario p = new Propietario();
        p.setUsuario(up);

        Inmueble i = new Inmueble();
        i.setId(1L);
        i.setPropietario(p);

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(i));

        mockMvc.perform(post("/actualizar-propiedad")
                .sessionAttr("usuario", u)
                .param("id", "1")
                .param("calle", "C")
                .param("numero", "1")
                .param("ciudad", "M")
                .param("codigoPostal", "28000")
                .param("precioNoche", "50")
                .param("capacidad", "2")
                .param("descripcion", "D"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/mis-propiedades"));

        verify(inmuebleDAO).save(i);
    }

    @Test
    void actualizarPropiedad_no_es_propietario() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        Usuario up = new Usuario();
        up.setId(2L);

        Propietario p = new Propietario();
        p.setUsuario(up);

        Inmueble i = new Inmueble();
        i.setId(1L);
        i.setPropietario(p);

        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(i));

        mockMvc.perform(post("/actualizar-propiedad")
                .sessionAttr("usuario", u)
                .param("id", "1")
                .param("calle", "C")
                .param("numero", "1")
                .param("ciudad", "M")
                .param("codigoPostal", "28000")
                .param("precioNoche", "50")
                .param("capacidad", "2")
                .param("descripcion", "D"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/mis-propiedades"));

        verify(inmuebleDAO, never()).save(any());
    }
}

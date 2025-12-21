package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.FavoritoDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@WebMvcTest(controllers = FavoritoController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoritoDAO favoritoDAO;

    @MockitoBean
    private InmuebleDAO inmuebleDAO;

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
                }
            };
        }
    }

    // ---------- MOSTRAR FAVORITOS ----------

    @Test
    void mostrarFavoritos_usuarioNoLogueado() throws Exception {
        mockMvc.perform(get("/favoritos"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/login?loginRequerido=true"));
    }

    @Test
    void mostrarFavoritos_filtraFavoritosInvalidos() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Usuario u = new Usuario();
        Propietario p = new Propietario();
        p.setUsuario(u);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(10L);
        inmueble.setPropietario(p);

        Favorito valido = new Favorito();
        valido.setInmueble(inmueble);

        Favorito invalido = new Favorito();

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(favoritoDAO.findByUsuario(usuario)).thenReturn(List.of(valido, invalido));
        when(inmuebleDAO.existsById(10L)).thenReturn(false);

        mockMvc.perform(get("/favoritos").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-deseos"))
                .andExpect(model().attribute("favoritos", List.of()));

        verify(inmuebleDAO).existsById(10L);
    }

    // ---------- AGREGAR FAVORITO ----------

    @Test
    void agregarFavorito_duplicado() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(10L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(inmuebleDAO.findById(10L)).thenReturn(Optional.of(inmueble));
        when(favoritoDAO.existsByUsuarioAndInmueble(usuario, inmueble)).thenReturn(true);

        mockMvc.perform(post("/favoritos/agregar/10").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/favoritos"));

        verify(favoritoDAO, never()).save(any());
    }

    @Test
    void agregarFavorito_correcto() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(10L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(inmuebleDAO.findById(10L)).thenReturn(Optional.of(inmueble));
        when(favoritoDAO.existsByUsuarioAndInmueble(usuario, inmueble)).thenReturn(false);

        mockMvc.perform(post("/favoritos/agregar/10").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/favoritos"));

        verify(favoritoDAO).save(isA(Favorito.class));
    }
}

package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.FavoritoDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FavoritoController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoritoDAO favoritoDAO;

    @MockBean
    private InmuebleDAO inmuebleDAO;

    // ---------- MOSTRAR FAVORITOS ----------

    @Test
    void mostrarFavoritos_usuarioNoLogueado() throws Exception {
        mockMvc.perform(get("/favoritos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?loginRequerido=true"));
    }

    @Test
    void mostrarFavoritos_usuarioLogueado() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(favoritoDAO.findByUsuario(usuario)).thenReturn(List.of());

        mockMvc.perform(get("/favoritos").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-deseos"))
                .andExpect(model().attributeExists("favoritos"));
    }

    // ---------- AGREGAR FAVORITO ----------

    @Test
    void agregarFavorito_usuarioNoLogueado() throws Exception {
        mockMvc.perform(post("/favoritos/agregar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?loginRequerido=true"));
    }

    @Test
    void agregarFavorito_correcto() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(10L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        when(inmuebleDAO.findById(10L)).thenReturn(java.util.Optional.of(inmueble));
        when(favoritoDAO.existsByUsuarioAndInmueble(usuario, inmueble)).thenReturn(false);

        mockMvc.perform(post("/favoritos/agregar/10").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favoritos"));
    }

    // ---------- ELIMINAR FAVORITO ----------

    @Test
    void eliminarFavorito_usuarioNoLogueado() throws Exception {
        mockMvc.perform(post("/favoritos/eliminar/5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?loginRequerido=true"));
    }

    @Test
    void eliminarFavorito_correcto() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuario);

        mockMvc.perform(post("/favoritos/eliminar/5").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favoritos"));
    }
}

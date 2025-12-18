package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpSession;

@WebMvcTest(controllers = MisPropiedadesController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
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

    @Test
    void mostrarMisPropiedades_redirige_si_no_logueado() throws Exception {
        mockMvc.perform(get("/mis-propiedades"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void mostrarMisPropiedades_muestra_propiedades_si_logueado() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Inmueble inmueble = new Inmueble();

        when(inmuebleDAO.findByPropietarioUsuarioId(1L))
                .thenReturn(List.of(inmueble));

        mockMvc.perform(get("/mis-propiedades")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("modificar-propiedad"))
                .andExpect(model().attributeExists("propiedades"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    void eliminarPropiedad_elimina_si_es_propietario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Usuario usuarioProp = new Usuario();
        usuarioProp.setId(1L);

        Propietario propietario = new Propietario();
        propietario.setUsuario(usuarioProp);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(10L);
        inmueble.setPropietario(propietario);

        when(inmuebleDAO.findById(10L)).thenReturn(Optional.of(inmueble));

        mockMvc.perform(get("/eliminar-propiedad/10")
                .sessionAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mis-propiedades"));

        verify(reservaDAO).deleteByInmuebleId(10L);
        verify(favoritoDAO).deleteByInmuebleId(10L);
        verify(inmuebleDAO).delete(inmueble);
    }

    @Test
    void editarPropiedad_redirige_si_no_es_propietario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);

        Propietario propietario = new Propietario();
        propietario.setUsuario(otroUsuario);

        Inmueble inmueble = new Inmueble();
        inmueble.setId(5L);
        inmueble.setPropietario(propietario);

        when(inmuebleDAO.findById(5L)).thenReturn(Optional.of(inmueble));

        mockMvc.perform(get("/editar-propiedad/5")
                .sessionAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mis-propiedades"));
    }
}

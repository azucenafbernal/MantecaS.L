package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

@WebMvcTest(controllers = ConfiguracionUsuarioController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class ConfiguracionUsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioDAO usuarioDAO;

    @MockitoBean
    private PropietarioDAO propietarioDAO;

    @MockitoBean
    private FavoritoDAO favoritoDAO;

    // ---------- ViewResolver dummy ----------
    @TestConfiguration
    static class TestViewResolverConfig {
        @Bean
        ViewResolver viewResolver() {
            return (String viewName, Locale locale) -> new AbstractView() {
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

    // ---------- GET CONFIGURACIÓN ----------

    @Test
    void mostrarConfiguracion_usuarioExiste() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(u));
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(null);

        mockMvc.perform(get("/configuracion").param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("configuracionusuario"));

        verify(usuarioDAO).findById(1L);
        verify(propietarioDAO).findByUsuarioId(1L);
    }

    @Test
    void mostrarConfiguracion_usuarioNoExiste() throws Exception {
        when(usuarioDAO.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/configuracion").param("idUsuario", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(usuarioDAO).findById(99L);
        verifyNoInteractions(propietarioDAO);
    }

    @Test
    void mostrarConfiguracion_excepcion() throws Exception {
        when(usuarioDAO.findById(1L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/configuracion").param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(usuarioDAO).findById(1L);
    }

    // ---------- ACTUALIZAR USUARIO ----------

    @Test
    void actualizarUsuario_correcto() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(u));

        mockMvc.perform(post("/configuracion/actualizarUsuario")
                .param("id", "1")
                .param("nombre", "Nuevo")
                .param("email", "test@test.com")
                .param("contrasena", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/configuracion?idUsuario=1"));

        verify(usuarioDAO).findById(1L);
        verify(usuarioDAO).save(u);
    }

    @Test
    void actualizarUsuario_usuarioNoExiste() throws Exception {
        when(usuarioDAO.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/configuracion/actualizarUsuario")
                .param("id", "1")
                .param("nombre", "Nuevo")
                .param("email", "a@a.com")
                .param("contrasena", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(usuarioDAO).findById(1L);
        verify(usuarioDAO, never()).save(any());
    }

    // ---------- ACTUALIZAR PROPIETARIO ----------

    @Test
    void actualizarPropietario_noEsPropietario() throws Exception {
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(null);

        mockMvc.perform(post("/configuracion/actualizarPropietario")
                .param("usuarioId", "1")
                .param("telefono", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(propietarioDAO).findByUsuarioId(1L);
    }

    // ---------- CUENTA BANCARIA ----------

    @Test
    void actualizarCuentaBancaria_cuentaVacia() throws Exception {
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(new Propietario());
        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(new Usuario()));

        mockMvc.perform(post("/configuracion/actualizarCuentaBancaria")
                .param("usuarioId", "1")
                .param("cuenta", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("configuracionusuario"));

        verify(propietarioDAO).findByUsuarioId(1L);
    }

    @Test
    void actualizarCuentaBancaria_ibanCorto() throws Exception {
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(new Propietario());
        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(new Usuario()));

        mockMvc.perform(post("/configuracion/actualizarCuentaBancaria")
                .param("usuarioId", "1")
                .param("cuenta", "ES12"))
                .andExpect(status().isOk())
                .andExpect(view().name("configuracionusuario"));
    }

    // ---------- TARJETA ----------

    @Test
    void actualizarTarjeta_numeroInvalido() throws Exception {
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(new Propietario());
        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(new Usuario()));

        mockMvc.perform(post("/configuracion/actualizarTarjeta")
                .param("usuarioId", "1")
                .param("creditcard", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("configuracionusuario"));
    }

    // ---------- CONTRASEÑA ----------

    @Test
    void cambiarContrasena_passwordIncorrecta() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setContrasena("correcta");

        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(u));

        mockMvc.perform(post("/configuracion/cambiarContrasena")
                .param("usuarioId", "1")
                .param("passwordActual", "mal")
                .param("passwordNueva", "nueva")
                .param("passwordConfirm", "nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("configuracionusuario"));

        verify(usuarioDAO, never()).save(any());
    }

    @Test
    void cambiarContrasena_noCoinciden() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setContrasena("actual");

        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(u));

        mockMvc.perform(post("/configuracion/cambiarContrasena")
                .param("usuarioId", "1")
                .param("passwordActual", "actual")
                .param("passwordNueva", "a")
                .param("passwordConfirm", "b"))
                .andExpect(status().isOk())
                .andExpect(view().name("configuracionusuario"));

        verify(usuarioDAO, never()).save(any());
    }

    // ---------- ELIMINAR CUENTA ----------

    @Test
    void eliminarCuenta_correcto() throws Exception {
        when(favoritoDAO.findByUsuarioId(1L)).thenReturn(List.of(new Favorito()));
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(null);

        mockMvc.perform(post("/configuracion/eliminarCuenta")
                .param("idUsuario", "1"))
                .andExpect(status().isOk());

        verify(favoritoDAO).findByUsuarioId(1L);
        verify(propietarioDAO).findByUsuarioId(1L);
    }

    @Test
    void eliminarCuenta_error() throws Exception {
        when(favoritoDAO.findByUsuarioId(1L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/configuracion/eliminarCuenta")
                .param("idUsuario", "1"))
                .andExpect(status().is5xxServerError());
    }

    // ---------- LOGOUT ----------

    @Test
    void logout_ok() throws Exception {
        mockMvc.perform(post("/configuracion/logout"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/"));
    }
}

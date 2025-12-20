package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
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

    @MockBean
    private UsuarioDAO usuarioDAO;

    @MockBean
    private PropietarioDAO propietarioDAO;

    @MockBean
    private FavoritoDAO favoritoDAO;

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
                    // This method is intentionally left empty because
                    // the test context does not require actual view rendering.
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
    }

    @Test
    void mostrarConfiguracion_usuarioNoExiste() throws Exception {
        when(usuarioDAO.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/configuracion").param("idUsuario", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void mostrarConfiguracion_excepcion() throws Exception {
        when(usuarioDAO.findById(1L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/configuracion").param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
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
    }

    // ---------- ELIMINAR CUENTA ----------
    @Test
    void eliminarCuenta_correcto() throws Exception {
        when(favoritoDAO.findByUsuarioId(1L)).thenReturn(List.of(new Favorito()));
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(null);

        mockMvc.perform(post("/configuracion/eliminarCuenta")
                .param("idUsuario", "1"))
                .andExpect(status().isOk());
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

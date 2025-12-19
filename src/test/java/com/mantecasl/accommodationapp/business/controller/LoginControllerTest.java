package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

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

@WebMvcTest(controllers = LoginController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioDAO usuarioDAO;

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

    // ---------- GET /login ----------

    @Test
    void mostrarLogin_usuario_no_logueado() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    void mostrarLogin_usuario_logueado_redirige() throws Exception {
        Usuario usuario = new Usuario();

        mockMvc.perform(get("/login")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/resultLogin"));
    }

    // ---------- POST /login ----------

    @Test
    void procesarLogin_usuario_no_existe() throws Exception {
        when(usuarioDAO.findByEmail("test@test.com")).thenReturn(null);

        mockMvc.perform(post("/login")
                .param("email", "test@test.com")
                .param("contrasena", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void procesarLogin_contrasena_incorrecta() throws Exception {
        Usuario existente = new Usuario();
        existente.setEmail("test@test.com");
        existente.setContrasena("correcta");

        when(usuarioDAO.findByEmail("test@test.com")).thenReturn(existente);

        mockMvc.perform(post("/login")
                .param("email", "test@test.com")
                .param("contrasena", "incorrecta"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void procesarLogin_correcto_sin_from() throws Exception {
        Usuario existente = new Usuario();
        existente.setEmail("test@test.com");
        existente.setContrasena("1234");

        when(usuarioDAO.findByEmail("test@test.com")).thenReturn(existente);

        mockMvc.perform(post("/login")
                .param("email", "test@test.com")
                .param("contrasena", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void procesarLogin_correcto_con_from() throws Exception {
        Usuario existente = new Usuario();
        existente.setEmail("test@test.com");
        existente.setContrasena("1234");

        when(usuarioDAO.findByEmail("test@test.com")).thenReturn(existente);

        mockMvc.perform(post("/login")
                .param("email", "test@test.com")
                .param("contrasena", "1234")
                .param("from", "/reservas/nueva/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/reservas/nueva/1"));
    }

    // ---------- GET /resultLogin ----------

    @Test
    void mostrarResultLogin_usuario_no_logueado() throws Exception {
        mockMvc.perform(get("/resultLogin"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void mostrarResultLogin_usuario_logueado() throws Exception {
        Usuario usuario = new Usuario();

        mockMvc.perform(get("/resultLogin")
                .sessionAttr("usuario", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("resultLogin"))
                .andExpect(model().attributeExists("usuario"));
    }

    // ---------- GET /logout ----------

    @Test
    void logout_ok() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/"));
    }
}

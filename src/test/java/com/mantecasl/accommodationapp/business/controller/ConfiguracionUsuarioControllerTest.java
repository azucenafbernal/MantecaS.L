package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.entity.Favorito;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.FavoritoDAO;
import com.mantecasl.accommodationapp.business.persistance.PropietarioDAO;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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

    // ---------- GET CONFIGURACIÓN ----------

    @Test
    void mostrarConfiguracion_usuarioExiste() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(u));
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(null);

        mockMvc.perform(get("/configuracion")
                .param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("configuracionusuario"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    void mostrarConfiguracion_usuarioNoExiste() throws Exception {
        when(usuarioDAO.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/configuracion")
                .param("idUsuario", "99"))
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
                .andExpect(status().is3xxRedirection());
    }

    // ---------- CAMBIAR CONTRASEÑA ----------

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

    // ---------- ELIMINAR CUENTA ----------

    @Test
    void eliminarCuenta_correcto() throws Exception {
        when(favoritoDAO.findByUsuarioId(1L)).thenReturn(List.of(new Favorito()));
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(null);

        mockMvc.perform(post("/configuracion/eliminarCuenta")
                .param("idUsuario", "1"))
                .andExpect(status().isOk());
    }
}

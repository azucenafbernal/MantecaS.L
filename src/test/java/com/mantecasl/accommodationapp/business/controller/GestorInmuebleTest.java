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

@WebMvcTest(controllers = GestorInmuebles.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@Import(GestorInmueblesTest.TestViewResolverConfig.class)
class GestorInmueblesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InmuebleDAO inmuebleDAO;

    @MockBean
    private UsuarioDAO usuarioDAO;

    @MockBean
    private PropietarioDAO propietarioDAO;

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

    // ---------- FORMULARIO ----------
    @Test
    void mostrarFormularioRegistro() throws Exception {
        mockMvc.perform(get("/propiedades/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro-propiedad"))
                .andExpect(model().attributeExists("inmueble"));
    }

    // ---------- REGISTRAR PROPIEDAD ----------
    @Test
    void registrarPropiedad_usuarioNoExiste() throws Exception {
        when(usuarioDAO.findByEmail("test@mail.com")).thenReturn(null);

        mockMvc.perform(post("/propiedades/registrar")
                .param("calle", "Calle A")
                .param("numero", "1")
                .param("ciudad", "Madrid")
                .param("codigoPostal", "28000")
                .param("precioNoche", "100")
                .param("descripcion", "Desc")
                .param("reservaDirecta", "true")
                .param("capacidad", "2")
                .param("emailPropietario", "test@mail.com")
                .param("telefonoContacto", "600")
                .param("cuentaBancaria", "ES123456789012345")
                .param("politicaCancelacion", "Flexible"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro-propiedad"));
    }

    @Test
    void registrarPropiedad_propietarioNuevo() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioDAO.findByEmail(any())).thenReturn(usuario);
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(null);
        when(propietarioDAO.save(any())).thenAnswer(i -> i.getArgument(0));
        when(inmuebleDAO.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/propiedades/registrar")
                .param("calle", "Calle A")
                .param("numero", "1")
                .param("ciudad", "Madrid")
                .param("codigoPostal", "28000")
                .param("precioNoche", "100")
                .param("descripcion", "Desc")
                .param("reservaDirecta", "true")
                .param("capacidad", "2")
                .param("emailPropietario", "test@mail.com")
                .param("telefonoContacto", "600")
                .param("cuentaBancaria", "ES123456789012345")
                .param("politicaCancelacion", "Flexible"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado-propiedad"));
    }

    @Test
    void registrarPropiedad_propietarioExistente() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Propietario propietario = new Propietario();
        propietario.setUsuario(usuario);

        when(usuarioDAO.findByEmail(any())).thenReturn(usuario);
        when(propietarioDAO.findByUsuarioId(1L)).thenReturn(propietario);
        when(inmuebleDAO.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/propiedades/registrar")
                .param("calle", "Calle B")
                .param("numero", "2")
                .param("ciudad", "Sevilla")
                .param("codigoPostal", "41000")
                .param("precioNoche", "80")
                .param("descripcion", "Desc")
                .param("reservaDirecta", "false")
                .param("capacidad", "3")
                .param("emailPropietario", "test@mail.com")
                .param("telefonoContacto", "600")
                .param("cuentaBancaria", "ES123456789012345")
                .param("politicaCancelacion", "Moderada"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado-propiedad"));
    }

    @Test
    void registrarPropiedad_excepcion() throws Exception {
        when(usuarioDAO.findByEmail(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/propiedades/registrar")
                .param("calle", "Calle A")
                .param("numero", "1")
                .param("ciudad", "Madrid")
                .param("codigoPostal", "28000")
                .param("precioNoche", "100")
                .param("descripcion", "Desc")
                .param("reservaDirecta", "true")
                .param("capacidad", "2")
                .param("emailPropietario", "test@mail.com")
                .param("telefonoContacto", "600")
                .param("cuentaBancaria", "ES123456789012345")
                .param("politicaCancelacion", "Flexible"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro-propiedad"));
    }

    // ---------- LISTAR ----------
    @Test
    void listarPropiedades() throws Exception {
        Inmueble inmueble = new Inmueble();
        inmueble.setPropietario(new Propietario());

        when(inmuebleDAO.findAll()).thenReturn(List.of(inmueble));

        mockMvc.perform(get("/propiedades"))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"));
    }

    // ---------- VER PROPIEDAD ----------
    @Test
    void verPropiedad_existente() throws Exception {
        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(new Inmueble()));

        mockMvc.perform(get("/propiedades/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-inmueble"));
    }
}

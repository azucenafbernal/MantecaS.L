package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.PropietarioDAO;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GestorInmuebles.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
class GestorInmueblesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InmuebleDAO inmuebleDAO;

    @MockBean
    private UsuarioDAO usuarioDAO;

    @MockBean
    private PropietarioDAO propietarioDAO;

    @Test
    void mostrarFormularioRegistro() throws Exception {
        mockMvc.perform(get("/propiedades/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro-propiedad"))
                .andExpect(model().attributeExists("inmueble"));
    }

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
                .param("telefonoContacto", "600000000")
                .param("cuentaBancaria", "ES123456789012345"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro-propiedad"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void registrarPropiedad_usuarioExiste_yPropietarioNuevo() throws Exception {
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
                .param("telefonoContacto", "600000000")
                .param("cuentaBancaria", "ES123456789012345"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado-propiedad"))
                .andExpect(model().attributeExists("mensaje"));
    }

    @Test
    void listarPropiedades() throws Exception {
        when(inmuebleDAO.findAll()).thenReturn(List.of(new Inmueble()));

        mockMvc.perform(get("/propiedades"))
                .andExpect(status().isOk())
                .andExpect(view().name("lista-propiedades"))
                .andExpect(model().attributeExists("propiedades"));
    }

    @Test
    void verPropiedad_existente() throws Exception {
        when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(new Inmueble()));

        mockMvc.perform(get("/propiedades/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle-inmueble"))
                .andExpect(model().attributeExists("inmueble"));
    }
}

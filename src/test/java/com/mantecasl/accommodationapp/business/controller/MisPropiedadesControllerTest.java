package com.mantecasl.accommodationapp.business.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mantecasl.accommodationapp.business.config.PropertiesDAOConfig;
import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    // ======= DEPENDENCIAS DEL CONTROLADOR =======

    @MockitoBean
    private PropertiesDAOConfig daoConfig;

    @MockitoBean
    private GestorNotificaciones gestorNotificaciones;

    @MockitoBean
    private ObjectProvider<MisPropiedadesController> selfProvider;

    // ======= DAOs USADOS A TRAVÉS DE daoConfig =======

    @MockitoBean
    private InmuebleDAO inmuebleDAO;

    @MockitoBean
    private ReservaDAO reservaDAO;

    @MockitoBean
    private SolicitudReservaDAO solicitudReservaDAO;

    @MockitoBean
    private FavoritoDAO favoritoDAO;

    @MockitoBean
    private DisponibilidadDAO disponibilidadDAO;

    @MockitoBean
    private NotificacionDAO notificacionDAO;

    @MockitoBean
    private InquilinoDAO inquilinoDAO;

    // ======= CONFIGURACIÓN VIEW RESOLVER =======

    static class TestViewResolverConfig {
        @Bean
        ViewResolver viewResolver() {
            return (String viewName, Locale locale) -> new AbstractView() {
                @Override
                protected void renderMergedOutputModel(
                        @NonNull Map<String, Object> model,
                        @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response) {
                }
            };
        }
    }

    // ======= SETUP COMÚN =======

    @BeforeEach
    void setup() {
        when(daoConfig.getInmuebleDAO()).thenReturn(inmuebleDAO);
        when(daoConfig.getReservaDAO()).thenReturn(reservaDAO);
        when(daoConfig.getSolicitudReservaDAO()).thenReturn(solicitudReservaDAO);
        when(daoConfig.getFavoritoDAO()).thenReturn(favoritoDAO);
        when(daoConfig.getDisponibilidadDAO()).thenReturn(disponibilidadDAO);
        when(daoConfig.getNotificacionDAO()).thenReturn(notificacionDAO);
        when(daoConfig.getInquilinoDAO()).thenReturn(inquilinoDAO);
    }

    // ===============================
    // MOSTRAR MIS PROPIEDADES
    // ===============================

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
                .andExpect(view().name("modificar-propiedad"))
                .andExpect(model().attributeExists("propiedades"));
    }

    // ===============================
    // EDITAR PROPIEDAD
    // ===============================

    @Test
    void editarPropiedad_ok() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        Usuario up = new Usuario();
        up.setId(1L);

        Propietario p = new Propietario();
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

    // ===============================
    // ACTUALIZAR PROPIEDAD
    // ===============================

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
                .param("politicaCancelacion", "Flexible"))
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
                .param("politicaCancelacion", "Flexible"))
                .andExpect(status().isOk())
                .andExpect(view().name("redirect:/mis-propiedades?success=true"));

        verify(inmuebleDAO).save(i);
    }
}

package com.mantecasl.accommodationapp.business.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.NotificacionDAO;

@SpringBootTest(classes = GestorNotificaciones.class)
class GestorNotificacionesTest {

    @MockitoBean
    private NotificacionDAO notificacionDAO;

    private GestorNotificaciones gestor;

    @BeforeEach
    void setUp() {
        gestor = new GestorNotificaciones(notificacionDAO, null);
    }

    // -------- HELPERS --------

    private Usuario usuario(String nombre) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(nombre + "@mail.com");
        u.setContrasena("x");
        return u;
    }

    private Inmueble inmueble(Propietario p) {
        Inmueble i = new Inmueble();
        i.setCalle("Calle");
        i.setNumero("1");
        i.setCiudad("Madrid");
        i.setCodigoPostal("28000");
        i.setPropietario(p);
        return i;
    }

    private SolicitudReserva solicitudBase() {
        Usuario propU = usuario("Prop");
        Usuario inqU = usuario("Inq");

        Propietario p = new Propietario();
        p.setUsuario(propU);

        Inmueble i = inmueble(p);

        Inquilino inq = new Inquilino();
        inq.setUsuario(inqU);
        inq.setMetodoPago("TARJETA");

        SolicitudReserva s = new SolicitudReserva();
        s.setId(1L);
        s.setInmueble(i);
        s.setInquilino(inq);
        s.setFechaInicio(Date.valueOf("2025-01-10"));
        s.setFechaFin(Date.valueOf("2025-01-15"));
        s.setFechaCreacion(LocalDateTime.now());
        return s;
    }

    private Reserva reservaBase(Inmueble i, Inquilino inq) {
        Reserva r = new Reserva();
        r.setId(5L);
        r.setInmueble(i);
        r.setInquilino(inq);
        r.setFechaInicio(Date.valueOf("2025-01-10"));
        r.setFechaFin(Date.valueOf("2025-01-15"));
        r.setPrecioTotal(200);
        return r;
    }

    // -------- CREACIÓN --------

    @Test
    void crearNotificacionSolicitudNueva() {
        when(notificacionDAO.save(isA(Notificacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertNotNull(gestor.crearNotificacionSolicitudNueva(solicitudBase()));
    }

    @Test
    void crearNotificacionSolicitudAprobada() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());
        s.setReserva(r);

        when(notificacionDAO.save(isA(Notificacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertNotNull(gestor.crearNotificacionSolicitudAprobada(s));
    }

    @Test
    void crearNotificacionSolicitudRechazada_motivoNull() {
        when(notificacionDAO.save(isA(Notificacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        Notificacion n = gestor.crearNotificacionSolicitudRechazada(solicitudBase(), null);

        assertTrue(n.getMensaje().contains("No especificado"));
    }

    @Test
    void crearNotificacionPagoDevuelto() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());

        when(notificacionDAO.save(isA(Notificacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertNotNull(gestor.crearNotificacionPagoDevuelto(r, 50));
    }

    @Test
    void crearNotificacionMensajePropietario() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());

        when(notificacionDAO.save(isA(Notificacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertNotNull(gestor.crearNotificacionMensajePropietario(r, "hola"));
    }

    // -------- ELIMINAR --------

    @Test
    void eliminarNotificacion() {
        gestor.eliminarNotificacion(1L);
        verify(notificacionDAO).deleteById(1L);
    }

    // -------- CONSULTAS --------

    @Test
    void obtenerNotificacionesUsuario() {
        Usuario u = usuario("A");

        when(notificacionDAO.findByUsuarioOrderByFechaCreacionDesc(u))
                .thenReturn(List.of(new Notificacion()));

        assertEquals(1, gestor.obtenerNotificacionesUsuario(u).size());
    }

    @Test
    void contarNotificacionesNoLeidas() {
        Usuario u = usuario("A");

        when(notificacionDAO.countByUsuarioAndLeidaFalse(u)).thenReturn(2L);

        assertEquals(2, gestor.contarNotificacionesNoLeidas(u));
        assertTrue(gestor.tieneNotificacionesNuevas(u));
    }

    // -------- MARCAR --------

    @Test
    void marcarComoLeida_existente() {
        Notificacion n = new Notificacion();

        when(notificacionDAO.findById(1L)).thenReturn(Optional.of(n));

        gestor.marcarComoLeida(1L);

        verify(notificacionDAO).save(n);
    }
}

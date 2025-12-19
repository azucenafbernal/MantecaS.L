package com.mantecasl.accommodationapp.business.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.NotificacionDAO;

@ExtendWith(MockitoExtension.class)
class GestorNotificacionesTest {

    @Mock
    private NotificacionDAO notificacionDAO;

    @InjectMocks
    private GestorNotificaciones gestor;

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
        i.setCalle("C");
        i.setNumero("1");
        i.setCiudad("Madrid");
        i.setCodigoPostal("28000");
        i.setPropietario(p);
        return i;
    }

    private SolicitudReserva solicitudBase() {
        Usuario prop = usuario("Prop");
        Usuario inqU = usuario("Inq");

        Propietario p = new Propietario();
        p.setUsuario(prop);

        Inmueble i = inmueble(p);

        Inquilino inq = new Inquilino();
        inq.setUsuario(inqU);
        inq.setMetodoPago("TARJETA");
        inq.setInmueble(i);

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
        when(notificacionDAO.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(gestor.crearNotificacionSolicitudNueva(solicitudBase()));
    }

    @Test
    void crearNotificacionSolicitudAprobada() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());
        s.setReserva(r);

        when(notificacionDAO.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(gestor.crearNotificacionSolicitudAprobada(s));
    }

    @Test
    void crearNotificacionSolicitudRechazada_motivoNull() {
        when(notificacionDAO.save(any())).thenAnswer(i -> i.getArgument(0));
        Notificacion n = gestor.crearNotificacionSolicitudRechazada(solicitudBase(), null);
        assertTrue(n.getMensaje().contains("No especificado"));
    }

    @Test
    void crearNotificacionPagoDevuelto() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());

        when(notificacionDAO.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(gestor.crearNotificacionPagoDevuelto(r, 50));
    }

    @Test
    void crearNotificacionReservaCanceladaPorPropietario() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());

        when(notificacionDAO.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(gestor.crearNotificacionReservaCanceladaPorPropietario(r, null));
    }

    @Test
    void crearNotificacionMensajePropietario() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());

        when(notificacionDAO.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(gestor.crearNotificacionMensajePropietario(r, "hola"));
    }

    // -------- NOTIFICACIONES MÚLTIPLES --------
    @Test
    void notificarEliminacionPropiedad() {
        Propietario p = new Propietario();
        p.setUsuario(usuario("Prop"));

        Inmueble i = inmueble(p);

        Reserva r = reservaBase(i, new Inquilino());
        r.getInquilino().setUsuario(usuario("Inq"));

        when(notificacionDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        gestor.notificarEliminacionPropiedad(i, List.of(r));

        verify(notificacionDAO, times(2)).save(any());
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
    void obtenerNotificacionesNoLeidas() {
        Usuario u = usuario("A");
        when(notificacionDAO.findByUsuarioAndLeidaFalseOrderByFechaCreacionDesc(u))
                .thenReturn(List.of(new Notificacion()));

        assertEquals(1, gestor.obtenerNotificacionesNoLeidas(u).size());
    }

    @Test
    void obtenerNotificacionesRecientes() {
        Usuario u = usuario("A");
        Notificacion n = new Notificacion();
        n.setFechaCreacion(LocalDateTime.now());

        when(notificacionDAO.findByUsuarioOrderByFechaCreacionDesc(u))
                .thenReturn(List.of(n));

        assertEquals(1, gestor.obtenerNotificacionesRecientes(u, 3).size());
    }

    @Test
    void contarNotificacionesNoLeidas() {
        Usuario u = usuario("A");
        when(notificacionDAO.countByUsuarioAndLeidaFalse(u)).thenReturn(2L);

        assertEquals(2, gestor.contarNotificacionesNoLeidas(u));
        assertTrue(gestor.tieneNotificacionesNuevas(u));
    }

    // -------- MARCAR / ELIMINAR --------
    @Test
    void marcarComoLeida_existente() {
        Notificacion n = new Notificacion();
        when(notificacionDAO.findById(1L)).thenReturn(Optional.of(n));

        gestor.marcarComoLeida(1L);
        verify(notificacionDAO).save(n);
    }

    @Test
    void eliminarNotificacion() {
        gestor.eliminarNotificacion(1L);
        verify(notificacionDAO).deleteById(1L);
    }

    @Test
    void eliminarTodasLasNotificaciones() {
        Usuario u = usuario("A");
        when(notificacionDAO.findByUsuarioOrderByFechaCreacionDesc(u))
                .thenReturn(List.of(new Notificacion(), new Notificacion()));

        gestor.eliminarTodasLasNotificaciones(u);
        verify(notificacionDAO).deleteAll(any());
    }

    // -------- WRAPPERS --------
    @Test
    void wrappers_ejecutan_metodos_base() {
        SolicitudReserva s = solicitudBase();
        when(notificacionDAO.save(any())).thenAnswer(i -> i.getArgument(0));

        assertNotNull(gestor.notificarNuevaSolicitudPropietario(s));
        assertNotNull(gestor.notificarRechazoInquilino(s, "x"));

        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());
        s.setReserva(r);
        assertNotNull(gestor.notificarAprobacionInquilino(s));
    }
}

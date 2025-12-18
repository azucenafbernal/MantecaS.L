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

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Inquilino;
import com.mantecasl.accommodationapp.business.entity.Notificacion;
import com.mantecasl.accommodationapp.business.entity.Propietario;
import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.entity.SolicitudReserva;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.NotificacionDAO;

@ExtendWith(MockitoExtension.class)
class GestorNotificacionesTest {

    @Mock
    private NotificacionDAO notificacionDAO;

    @InjectMocks
    private GestorNotificaciones gestorNotificaciones;

    // ---------------- HELPERS ----------------
    private Usuario usuario(String nombre, String email) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setContrasena("x");
        return u;
    }

    private Propietario propietarioConUsuario(Usuario u) {
        Propietario p = new Propietario();
        p.setUsuario(u);
        return p;
    }

    private Inmueble inmuebleConPropietario(Propietario p) {
        Inmueble i = new Inmueble();
        i.setCalle("Calle Falsa");
        i.setNumero("123");
        i.setCiudad("Madrid");
        i.setCodigoPostal("28000");
        i.setPrecioNoche(50.0);
        i.setDescripcion("Desc");
        i.setCapacidad(2);
        i.setPropietario(p);
        return i;
    }

    private Inquilino inquilinoConUsuario(Usuario u) {
        Inquilino inq = new Inquilino();
        inq.setUsuario(u);
        inq.setTelefono("600000000");
        inq.setDocumentoIdentidad("DNI");
        inq.setMetodoPago("TARJETA");
        return inq;
    }

    private SolicitudReserva solicitudBase() {
        Usuario uProp = usuario("Propietario", "prop@x.com");
        Usuario uInq = usuario("Inquilino", "inq@x.com");

        Propietario p = propietarioConUsuario(uProp);
        Inmueble inm = inmuebleConPropietario(p);

        Inquilino inq = inquilinoConUsuario(uInq);
        inq.setInmueble(inm);

        SolicitudReserva s = new SolicitudReserva();
        s.setId(10L);
        s.setInmueble(inm);
        s.setInquilino(inq);
        s.setFechaInicio(Date.valueOf("2025-01-10"));
        s.setFechaFin(Date.valueOf("2025-01-15"));
        s.setPrecioTotal(250.0);
        s.setEstado("PENDIENTE");
        s.setFechaCreacion(LocalDateTime.now());
        return s;
    }

    private Reserva reservaBase(Inmueble inm, Inquilino inq) {
        Reserva r = new Reserva();
        r.setId(77L);
        r.setInmueble(inm);
        r.setInquilino(inq);
        r.setFechaInicio(Date.valueOf("2025-01-10"));
        r.setFechaFin(Date.valueOf("2025-01-15"));
        r.setPrecioTotal(250.0);
        r.setEstado("CONFIRMADA");
        return r;
    }

    // ---------------- TESTS (metodología: equivalencia, límites, errores)
    // ----------------

    @Test
    void crearNotificacionSolicitudRechazada_motivoNull_usaNoEspecificado_y_guarda() {
        SolicitudReserva s = solicitudBase();

        when(notificacionDAO.save(any(Notificacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notificacion n = gestorNotificaciones.crearNotificacionSolicitudRechazada(s, null);

        assertNotNull(n);
        assertEquals(Notificacion.SOLICITUD_RECHAZADA, n.getTipo());
        assertEquals("❌ Solicitud de reserva RECHAZADA", n.getTitulo());
        assertNotNull(n.getUsuario());
        assertTrue(n.getMensaje().contains("Motivo: No especificado"));

        verify(notificacionDAO, times(1)).save(any(Notificacion.class));
    }

    @Test
    void crearNotificacionSolicitudRechazada_motivoNoNull_lo_incluye_en_mensaje() {
        SolicitudReserva s = solicitudBase();

        when(notificacionDAO.save(any(Notificacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notificacion n = gestorNotificaciones.crearNotificacionSolicitudRechazada(s, "Porque sí");

        assertNotNull(n);
        assertTrue(n.getMensaje().contains("Motivo: Porque sí"));
        verify(notificacionDAO, times(1)).save(any(Notificacion.class));
    }

    @Test
    void crearNotificacionSolicitudNueva_setea_accionUrl_y_campos_clave() {
        SolicitudReserva s = solicitudBase();

        when(notificacionDAO.save(any(Notificacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notificacion n = gestorNotificaciones.crearNotificacionSolicitudNueva(s);

        assertEquals(Notificacion.SOLICITUD_NUEVA, n.getTipo());
        assertEquals("/propietario/solicitudes/10", n.getAccionUrl());
        assertNotNull(n.getInmueble());
        assertNotNull(n.getSolicitud());
        assertNotNull(n.getUsuario()); // propietario
        verify(notificacionDAO, times(1)).save(any(Notificacion.class));
    }

    @Test
    void crearNotificacionSolicitudAprobada_asignaReserva_y_accionUrl_correcta() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());
        s.setEstado("APROBADA");
        s.setReserva(r);

        when(notificacionDAO.save(any(Notificacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notificacion n = gestorNotificaciones.crearNotificacionSolicitudAprobada(s);

        assertEquals(Notificacion.SOLICITUD_APROBADA, n.getTipo());
        assertNotNull(n.getReserva());
        assertEquals("/reservas/77", n.getAccionUrl());
        assertNotNull(n.getUsuario()); // inquilino
        verify(notificacionDAO, times(1)).save(any(Notificacion.class));
    }

    @Test
    void crearNotificacionReservaDirecta_crea_dos_notificaciones_y_hace_dos_saves() {
        SolicitudReserva s = solicitudBase();
        Reserva r = reservaBase(s.getInmueble(), s.getInquilino());

        when(notificacionDAO.save(any(Notificacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        gestorNotificaciones.crearNotificacionReservaDirecta(r);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionDAO, times(2)).save(captor.capture());

        List<Notificacion> guardadas = captor.getAllValues();
        assertEquals(2, guardadas.size());

        Notificacion n1 = guardadas.get(0);
        Notificacion n2 = guardadas.get(1);

        assertEquals(Notificacion.RESERVA_CONFIRMADA, n1.getTipo());
        assertEquals(Notificacion.RESERVA_CONFIRMADA, n2.getTipo());

        assertNotNull(n1.getUsuario());
        assertNotNull(n2.getUsuario());

        assertTrue(n1.getAccionUrl().contains("/reservas/77"));
        assertTrue(n2.getAccionUrl().contains("/propietario/reservas/77"));
    }

    @Test
    void obtenerUltimasNotificaciones_limite_menor_que_tamano_devuelve_sublista() {
        Usuario u = usuario("A", "a@a.com");

        when(notificacionDAO.findByUsuarioOrderByFechaCreacionDesc(u))
                .thenReturn(List.of(new Notificacion(), new Notificacion(), new Notificacion()));

        List<Notificacion> res = gestorNotificaciones.obtenerUltimasNotificaciones(u, 2);

        assertEquals(2, res.size());
        verify(notificacionDAO, times(1)).findByUsuarioOrderByFechaCreacionDesc(u);
    }

    @Test
    void marcarComoLeida_si_existe_setea_true_y_guarda() {
        Notificacion n = new Notificacion();
        n.setId(5L);
        n.setLeida(false);

        when(notificacionDAO.findById(5L)).thenReturn(Optional.of(n));

        gestorNotificaciones.marcarComoLeida(5L);

        assertTrue(n.isLeida());
        verify(notificacionDAO, times(1)).save(n);
    }

    @Test
    void marcarComoLeida_si_no_existe_no_guarda() {
        when(notificacionDAO.findById(999L)).thenReturn(Optional.empty());

        gestorNotificaciones.marcarComoLeida(999L);

        verify(notificacionDAO, never()).save(any());
    }

    @Test
    void marcarTodasComoLeidas_marca_todas_y_saveAll() {
        Usuario u = usuario("U", "u@u.com");

        Notificacion n1 = new Notificacion();
        n1.setLeida(false);
        Notificacion n2 = new Notificacion();
        n2.setLeida(false);

        when(notificacionDAO.findByUsuarioAndLeidaFalseOrderByFechaCreacionDesc(u))
                .thenReturn(List.of(n1, n2));

        gestorNotificaciones.marcarTodasComoLeidas(u);

        assertTrue(n1.isLeida());
        assertTrue(n2.isLeida());
        verify(notificacionDAO, times(1)).saveAll(List.of(n1, n2));
    }
}

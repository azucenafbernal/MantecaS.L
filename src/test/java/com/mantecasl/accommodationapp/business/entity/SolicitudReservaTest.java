package com.mantecasl.accommodationapp.business.entity;

import java.sql.Date;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SolicitudReservaTest {

    private Inquilino inquilino;
    private Inmueble inmueble;

    @BeforeEach
    void setUp() {
        Usuario u = new Usuario("Ana", "ana@example.com", "pass");
        inquilino = new Inquilino(u, "611111111", "A1234567", "PAYPAL");
        inmueble = new Inmueble();
    }

    @Test
    void constructorPorDefecto_inicializaPendienteYFechaCreacion() {
        SolicitudReserva s = new SolicitudReserva();

        assertEquals("PENDIENTE", s.getEstado());
        assertNotNull(s.getFechaCreacion());
        assertTrue(s.getFechaCreacion().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void constructorCompleto_asignaValoresCorrectamente() {
        Date inicio = Date.valueOf("2025-01-10");
        Date fin = Date.valueOf("2025-01-12");

        SolicitudReserva s = new SolicitudReserva(inquilino, inmueble, inicio, fin, 120.0);

        assertEquals(inquilino, s.getInquilino());
        assertEquals(inmueble, s.getInmueble());
        assertEquals(120.0, s.getPrecioTotal());
        assertEquals("PENDIENTE", s.getEstado());
    }

    @Test
    void getNumeroNoches_funcionaCorrectamente() {
        SolicitudReserva s = new SolicitudReserva();
        s.setFechaInicio(Date.valueOf("2025-01-01"));
        s.setFechaFin(Date.valueOf("2025-01-04"));

        assertEquals(3, s.getNumeroNoches());
    }

    @Test
    void aprobar_cambiaEstadoYMensajeYFechaDecision() {
        SolicitudReserva s = new SolicitudReserva();
        s.aprobar("Todo correcto");

        assertEquals("APROBADA", s.getEstado());
        assertEquals("Todo correcto", s.getMensajePropietario());
        assertNotNull(s.getFechaDecision());
    }

    @Test
    void rechazar_cambiaEstadoYMotivoYFechaDecision() {
        SolicitudReserva s = new SolicitudReserva();
        s.rechazar("Fechas no disponibles");

        assertEquals("RECHAZADA", s.getEstado());
        assertEquals("Fechas no disponibles", s.getMotivoRechazo());
        assertNotNull(s.getFechaDecision());
    }

    @Test
    void cancelar_equivaleARechazar() {
        SolicitudReserva s = new SolicitudReserva();
        s.cancelar("Motivo de cancelación");

        assertEquals("RECHAZADA", s.getEstado());
        assertEquals("Motivo de cancelación", s.getMotivoRechazo());
    }

    @Test
    void crearReserva_lanzaExcepcionSiNoEstaAprobada() {
        SolicitudReserva s = new SolicitudReserva();
        s.setEstado("PENDIENTE");

        assertThrows(IllegalStateException.class, s::crearReserva);
    }

    @Test
    void crearReserva_funcionaCorrectamenteSiAprobada() {
        Date inicio = Date.valueOf("2025-03-01");
        Date fin = Date.valueOf("2025-03-05");

        SolicitudReserva s = new SolicitudReserva(inquilino, inmueble, inicio, fin, 300.0);
        s.setEstado("APROBADA");

        Reserva r = s.crearReserva();

        assertNotNull(r);
        assertEquals(inquilino, r.getInquilino());
        assertEquals(inmueble, r.getInmueble());
        assertEquals(inicio, r.getFechaInicio());
        assertEquals(fin, r.getFechaFin());
        assertEquals(300.0, r.getPrecioTotal());
        assertEquals("CONFIRMADA", r.getEstado());
        assertEquals(r, s.getReserva());
    }

    @Test
    void settersYGettersFuncionan() {
        SolicitudReserva s = new SolicitudReserva();

        s.setObservacionesInquilino("Observación X");
        assertEquals("Observación X", s.getObservacionesInquilino());

        s.setMensajePropietario("Mensaje X");
        assertEquals("Mensaje X", s.getMensajePropietario());

        s.setMotivoRechazo("Motivo X");
        assertEquals("Motivo X", s.getMotivoRechazo());
    }

}

package com.mantecasl.accommodationapp.business.controller;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.isA;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mantecasl.accommodationapp.business.entity.Disponibilidad;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.DisponibilidadDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;
import com.mantecasl.accommodationapp.business.persistance.SolicitudReservaDAO;

@ExtendWith(MockitoExtension.class)
class GestorDisponibilidadTest {

        @Mock
        private DisponibilidadDAO disponibilidadDAO;

        @Mock
        private ReservaDAO reservaDAO;

        @Mock
        private InmuebleDAO inmuebleDAO;

        @Mock
        private SolicitudReservaDAO solicitudReservaDAO;

        @InjectMocks
        private GestorDisponibilidad gestor;

        // ---------- VALIDAR FECHAS ----------

        @Test
        void validarFechas_correctas() {
                Date[] fechas = gestor.validarFechas("2025-01-10", "2025-01-15");

                assertEquals(Date.valueOf("2025-01-10"), fechas[0]);
                assertEquals(Date.valueOf("2025-01-15"), fechas[1]);
        }

        @Test
        void validarFechas_null_lanzaExcepcion() {
                assertThrows(RuntimeException.class, () -> gestor.validarFechas(null, null));
        }

        @Test
        void validarFechas_formatoInvalido() {
                assertThrows(RuntimeException.class,
                                () -> gestor.validarFechas("10-01-2025", "15-01-2025"));
        }

        @Test
        void validarFechas_finAntes() {
                assertThrows(RuntimeException.class,
                                () -> gestor.validarFechas("2025-01-15", "2025-01-10"));
        }

        @Test
        void validarFechas_mismoDia() {
                assertThrows(RuntimeException.class,
                                () -> gestor.validarFechas("2025-01-10", "2025-01-10"));
        }

        // ---------- CREAR RESERVA ----------

        @Test
        void crearReserva_correcta() {
                Inmueble inmueble = new Inmueble();
                inmueble.setId(1L);

                when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));
                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of());
                when(disponibilidadDAO.save(isA(Disponibilidad.class)))
                                .thenAnswer(i -> i.getArgument(0));

                Disponibilidad d = gestor.crearReserva(
                                1L,
                                Date.valueOf("2025-01-10"),
                                Date.valueOf("2025-01-15"),
                                true);

                assertNotNull(d);
                assertFalse(d.isDisponible());

                verify(disponibilidadDAO).save(isA(Disponibilidad.class));
        }

        @Test
        void crearReserva_inmuebleNoExiste() {
                when(inmuebleDAO.findById(1L)).thenReturn(Optional.empty());

                assertThrows(RuntimeException.class, this::callCrearReservaInmuebleNoExiste);
        }

        private void callCrearReservaInmuebleNoExiste() {
                gestor.crearReserva(1L, Date.valueOf("2025-01-10"), Date.valueOf("2025-01-15"), false);
        }

        @Test
        void crearReserva_noDisponible() {
                Inmueble inmueble = new Inmueble();
                inmueble.setId(1L);

                Disponibilidad d = new Disponibilidad();
                d.setFechaInicio(Date.valueOf("2025-01-12"));
                d.setFechaFin(Date.valueOf("2025-01-20"));

                when(inmuebleDAO.findById(1L)).thenReturn(Optional.of(inmueble));
                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of(d));

                assertThrows(RuntimeException.class, this::callCrearReservaNoDisponible);
        }

        private void callCrearReservaNoDisponible() {
                gestor.crearReserva(1L, Date.valueOf("2025-01-10"), Date.valueOf("2025-01-15"), false);
        }

        // ---------- VERIFICAR DISPONIBILIDAD ----------

        @Test
        void verificarDisponibilidad_sinSolapamientos() {
                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of());

                assertTrue(gestor.verificarDisponibilidad(
                                1L,
                                Date.valueOf("2025-01-10"),
                                Date.valueOf("2025-01-15")));
        }

        @Test
        void verificarDisponibilidad_conSolapamiento() {
                Disponibilidad d = new Disponibilidad();
                d.setFechaInicio(Date.valueOf("2025-01-12"));
                d.setFechaFin(Date.valueOf("2025-01-20"));

                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of(d));

                assertFalse(gestor.verificarDisponibilidad(
                                1L,
                                Date.valueOf("2025-01-10"),
                                Date.valueOf("2025-01-15")));
        }

        // ---------- DISPONIBILIDAD PARA SOLICITUD ----------

        @Test
        void verificarDisponibilidadParaSolicitud_disponible() {
                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of());
                when(reservaDAO.findByInmuebleIdAndEstado(1L, "CONFIRMADA"))
                                .thenReturn(List.of());
                when(solicitudReservaDAO.findByInmuebleIdAndEstado(1L, "APROBADA"))
                                .thenReturn(List.of());

                assertTrue(gestor.verificarDisponibilidadParaSolicitud(
                                1L,
                                Date.valueOf("2025-01-10"),
                                Date.valueOf("2025-01-15")));
        }

        @Test
        void verificarDisponibilidadParaSolicitud_conSolapamiento() {
                Disponibilidad d = new Disponibilidad();
                d.setFechaInicio(Date.valueOf("2025-01-10"));
                d.setFechaFin(Date.valueOf("2025-01-20"));

                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of(d));
                when(reservaDAO.findByInmuebleIdAndEstado(1L, "CONFIRMADA"))
                                .thenReturn(List.of());
                when(solicitudReservaDAO.findByInmuebleIdAndEstado(1L, "APROBADA"))
                                .thenReturn(List.of());

                assertFalse(gestor.verificarDisponibilidadParaSolicitud(
                                1L,
                                Date.valueOf("2025-01-12"),
                                Date.valueOf("2025-01-13")));
        }

        // ---------- BUSCAR INMUEBLES DISPONIBLES ----------

        @Test
        void buscarInmueblesDisponibles_filtraTodo() {
                Inmueble i = new Inmueble();
                i.setId(1L);
                i.setCiudad("Madrid");
                i.setCapacidad(4);

                when(inmuebleDAO.findAll()).thenReturn(List.of(i));
                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of());

                List<Inmueble> resultado = gestor.buscarInmueblesDisponibles(
                                Date.valueOf("2025-01-10"),
                                Date.valueOf("2025-01-15"),
                                "Madrid",
                                2);

                assertEquals(1, resultado.size());
        }

        @Test
        void buscarInmueblesDisponibles_noCumpleFiltros() {
                Inmueble i = new Inmueble();
                i.setId(1L);
                i.setCiudad("Barcelona");
                i.setCapacidad(1);

                when(inmuebleDAO.findAll()).thenReturn(List.of(i));
                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of());

                List<Inmueble> resultado = gestor.buscarInmueblesDisponibles(
                                Date.valueOf("2025-01-10"),
                                Date.valueOf("2025-01-15"),
                                "Madrid",
                                2);

                assertTrue(resultado.isEmpty());
        }

        // ---------- OBTENER RESERVAS ----------

        @Test
        void obtenerReservasPorInmueble() {
                when(disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(1L))
                                .thenReturn(List.of(new Disponibilidad()));

                List<Disponibilidad> res = gestor.obtenerReservasPorInmueble(1L);

                assertEquals(1, res.size());
        }
}

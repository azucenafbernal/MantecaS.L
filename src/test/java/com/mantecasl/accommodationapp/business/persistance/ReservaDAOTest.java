package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.mantecasl.accommodationapp.business.entity.*;

@DataJpaTest
class ReservaDAOTest {

       @Autowired
       private ReservaDAO reservaDAO;

       @Autowired
       private InmuebleDAO inmuebleDAO;

       @Autowired
       private UsuarioDAO usuarioDAO;

       @Autowired
       private InquilinoDAO inquilinoDAO;

       private Reserva crearReserva(String estado, LocalDate inicio, LocalDate fin) {
              Usuario usuario = new Usuario();
              usuario.setNombre("User");
              usuario.setEmail("u" + Math.random() + "@test.com");
              usuario.setContrasena("1234");
              usuario = usuarioDAO.save(usuario);

              Inmueble inmueble = new Inmueble();
              inmueble.setCiudad("Madrid");
              inmueble.setPrecioNoche(50);
              inmueble = inmuebleDAO.save(inmueble);

              Inquilino inquilino = new Inquilino(usuario, "600000000", "DOC" + Math.random(), "TARJETA");
              inquilino.setInmueble(inmueble);
              inquilino = inquilinoDAO.save(inquilino);

              Reserva reserva = new Reserva(
                            inmueble,
                            inquilino,
                            Date.valueOf(inicio),
                            Date.valueOf(fin),
                            100);
              reserva.setEstado(estado);
              return reservaDAO.save(reserva);
       }

       @Test
       void findByInmuebleId() {
              Reserva r = crearReserva("CONFIRMADA", LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

              List<Reserva> resultado = reservaDAO.findByInmuebleId(r.getInmueble().getId());
              assertThat(resultado).hasSize(1);
       }

       @Test
       void findByInquilinoUsuarioId() {
              Reserva r = crearReserva("CONFIRMADA", LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

              List<Reserva> resultado = reservaDAO.findByInquilinoUsuarioId(r.getInquilino().getUsuario().getId());

              assertThat(resultado).hasSize(1);
       }

       @Test
       void findByInmuebleIdAndEstado() {
              Reserva r = crearReserva("CONFIRMADA", LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

              List<Reserva> resultado = reservaDAO.findByInmuebleIdAndEstado(r.getInmueble().getId(), "CONFIRMADA");

              assertThat(resultado).hasSize(1);
       }

       @Test
       void findReservasFuturasByInmueble() {
              Reserva r = crearReserva("CONFIRMADA", LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));

              List<Reserva> resultado = reservaDAO.findReservasFuturasByInmueble(r.getInmueble().getId());

              assertThat(resultado).hasSize(1);
       }

       @Test
       void findReservasConfirmadasFuturasByInmueble() {
              Reserva r = crearReserva("CONFIRMADA", LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));

              List<Reserva> resultado = reservaDAO.findReservasConfirmadasFuturasByInmueble(r.getInmueble().getId());

              assertThat(resultado).hasSize(1);
       }

       @Test
       void findReservasConfirmadasFuturasDesdeFecha() {
              Reserva r = crearReserva("CONFIRMADA", LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));

              List<Reserva> resultado = reservaDAO.findReservasConfirmadasFuturasDesdeFecha(
                            r.getInmueble().getId(),
                            Date.valueOf(LocalDate.now()));

              assertThat(resultado).hasSize(1);
       }

       @Test
       void findReservasActivasEnRango() {
              Reserva r = crearReserva("CONFIRMADA", LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));

              List<Reserva> resultado = reservaDAO.findReservasActivasEnRango(
                            r.getInmueble().getId(),
                            Date.valueOf(LocalDate.now().plusDays(1)),
                            Date.valueOf(LocalDate.now().plusDays(5)));

              assertThat(resultado).hasSize(1);
       }

       @Test
       @Transactional
       void deleteByInmuebleId() {
              Reserva r = crearReserva("CONFIRMADA", LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

              reservaDAO.deleteByInmuebleId(r.getInmueble().getId());

              List<Reserva> resultado = reservaDAO.findByInmuebleId(r.getInmueble().getId());
              assertThat(resultado).isEmpty();
       }
}

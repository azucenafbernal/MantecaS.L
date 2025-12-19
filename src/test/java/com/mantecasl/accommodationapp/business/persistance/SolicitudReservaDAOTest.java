package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mantecasl.accommodationapp.business.entity.*;

@DataJpaTest
class SolicitudReservaDAOTest {

    @Autowired
    private SolicitudReservaDAO solicitudReservaDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private InquilinoDAO inquilinoDAO;

    @Autowired
    private PropietarioDAO propietarioDAO;

    private SolicitudReserva crearSolicitud(String estado) {
        Usuario userProp = new Usuario();
        userProp.setNombre("Prop");
        userProp.setEmail("prop" + Math.random() + "@test.com");
        userProp.setContrasena("1234");
        userProp = usuarioDAO.save(userProp);

        Propietario propietario = new Propietario();
        propietario.setUsuario(userProp);
        propietario = propietarioDAO.save(propietario);

        Inmueble inmueble = new Inmueble();
        inmueble.setCiudad("Madrid");
        inmueble.setPrecioNoche(100);
        inmueble.setPropietario(propietario);
        inmueble = inmuebleDAO.save(inmueble);

        Usuario userInq = new Usuario();
        userInq.setNombre("Inq");
        userInq.setEmail("inq" + Math.random() + "@test.com");
        userInq.setContrasena("1234");
        userInq = usuarioDAO.save(userInq);

        Inquilino inquilino = new Inquilino(userInq, "600000000", "DOC" + Math.random(), "TARJETA");
        inquilino.setInmueble(inmueble);
        inquilino = inquilinoDAO.save(inquilino);

        SolicitudReserva solicitud = new SolicitudReserva(
                inquilino,
                inmueble,
                Date.valueOf(LocalDate.now().plusDays(1)),
                Date.valueOf(LocalDate.now().plusDays(3)),
                200);
        solicitud.setEstado(estado);

        return solicitudReservaDAO.save(solicitud);
    }

    @Test
    void findByEstado() {
        crearSolicitud("PENDIENTE");

        List<SolicitudReserva> res = solicitudReservaDAO.findByEstado("PENDIENTE");
        assertThat(res).isNotEmpty();
    }

    @Test
    void findByInmuebleId() {
        SolicitudReserva s = crearSolicitud("PENDIENTE");

        List<SolicitudReserva> res = solicitudReservaDAO.findByInmuebleId(s.getInmueble().getId());

        assertThat(res).hasSize(1);
    }

    @Test
    void findByInmuebleIdAndEstado() {
        SolicitudReserva s = crearSolicitud("PENDIENTE");

        List<SolicitudReserva> res = solicitudReservaDAO.findByInmuebleIdAndEstado(
                s.getInmueble().getId(), "PENDIENTE");

        assertThat(res).hasSize(1);
    }

    @Test
    void findByPropietarioId() {
        SolicitudReserva s = crearSolicitud("PENDIENTE");

        List<SolicitudReserva> res = solicitudReservaDAO.findByPropietarioId(
                s.getInmueble().getPropietario().getId());

        assertThat(res).hasSize(1);
    }

    @Test
    void findPendientesByPropietarioId() {
        SolicitudReserva s = crearSolicitud("PENDIENTE");

        List<SolicitudReserva> res = solicitudReservaDAO.findPendientesByPropietarioId(
                s.getInmueble().getPropietario().getId());

        assertThat(res).hasSize(1);
    }

    @Test
    void findByInmueblePropietarioId_variantes() {
        SolicitudReserva s = crearSolicitud("PENDIENTE");
        Long propId = s.getInmueble().getPropietario().getId();

        assertThat(solicitudReservaDAO.findByInmueblePropietarioId(propId)).hasSize(1);
        assertThat(solicitudReservaDAO.findByInmueble_PropietarioId(propId)).hasSize(1);
        assertThat(solicitudReservaDAO.findByInmueblePropietarioIdAndEstado(propId, "PENDIENTE")).hasSize(1);
        assertThat(solicitudReservaDAO.findByInmueble_PropietarioIdAndEstado(propId, "PENDIENTE")).hasSize(1);
    }

    @Test
    void findPendientesByInmuebleId() {
        SolicitudReserva s = crearSolicitud("PENDIENTE");

        List<SolicitudReserva> res = solicitudReservaDAO.findPendientesByInmuebleId(
                s.getInmueble().getId());

        assertThat(res).hasSize(1);
    }

    @Test
    void countByInmueblePropietarioIdAndEstado() {
        SolicitudReserva s = crearSolicitud("PENDIENTE");

        long count = solicitudReservaDAO.countByInmueblePropietarioIdAndEstado(
                s.getInmueble().getPropietario().getId(), "PENDIENTE");

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByInquilinoId() {
        SolicitudReserva s = crearSolicitud("PENDIENTE");

        List<SolicitudReserva> res = solicitudReservaDAO.findByInquilinoId(
                s.getInquilino().getId());

        assertThat(res).hasSize(1);
    }
}

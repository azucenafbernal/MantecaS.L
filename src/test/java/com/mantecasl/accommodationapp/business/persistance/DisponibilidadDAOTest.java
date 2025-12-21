package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import com.mantecasl.accommodationapp.business.entity.Disponibilidad;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Propietario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // H2 en memoria
class DisponibilidadDAOTest {

    @Autowired
    private DisponibilidadDAO disponibilidadDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private PropietarioDAO propietarioDAO;

    @Test
    void testTodosLosMetodosDelRepositorio() {
        // ---------- SETUP ----------
        Propietario propietario = new Propietario();
        propietario = propietarioDAO.save(propietario);

        Inmueble inmueble = new Inmueble();
        inmueble.setCiudad("Madrid");
        inmueble.setCapacidad(4);
        inmueble.setPropietario(propietario);
        inmueble = inmuebleDAO.save(inmueble);

        Date inicio = Date.valueOf(LocalDate.now().plusDays(1));
        Date fin = Date.valueOf(LocalDate.now().plusDays(5));

        Disponibilidad disponible = new Disponibilidad();
        disponible.setInmueble(inmueble);
        disponible.setFechaInicio(inicio);
        disponible.setFechaFin(fin);
        disponible.setDisponible(true);

        Disponibilidad bloqueada = new Disponibilidad();
        bloqueada.setInmueble(inmueble);
        bloqueada.setFechaInicio(inicio);
        bloqueada.setFechaFin(fin);
        bloqueada.setDisponible(false);

        disponibilidadDAO.save(disponible);
        disponibilidadDAO.save(bloqueada);

        Long inmuebleId = inmueble.getId();

        // ---------- EJECUCIÓN ----------
        List<Disponibilidad> todas = disponibilidadDAO.findByInmuebleId(inmuebleId);

        List<Disponibilidad> disponibles = disponibilidadDAO.findByInmuebleIdAndDisponibleTrue(inmuebleId);

        List<Disponibilidad> bloqueadas = disponibilidadDAO.findByInmuebleIdAndDisponibleFalse(inmuebleId);

        List<Disponibilidad> rango = disponibilidadDAO.findDisponiblesEnRango(
                inmuebleId,
                Date.valueOf(LocalDate.now()),
                Date.valueOf(LocalDate.now().plusDays(10)));

        // ---------- ASSERTS ----------
        assertThat(todas).hasSize(2);
        assertThat(disponibles).hasSize(1);
        assertThat(bloqueadas).hasSize(1);
        assertThat(rango).hasSize(1);
    }
}

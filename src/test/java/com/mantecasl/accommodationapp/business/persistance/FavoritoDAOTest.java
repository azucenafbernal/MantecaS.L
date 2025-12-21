package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import com.mantecasl.accommodationapp.business.entity.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class FavoritoDAOTest {

    @Autowired
    private FavoritoDAO favoritoDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private PropietarioDAO propietarioDAO;

    @Test
    void testTodosLosMetodosFavoritoDAO() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        Propietario propietario = new Propietario();
        propietario = propietarioDAO.save(propietario);

        Inmueble inmueble = new Inmueble();
        inmueble.setCiudad("Madrid");
        inmueble.setCapacidad(2);
        inmueble.setPropietario(propietario);
        inmueble = inmuebleDAO.save(inmueble);

        Favorito favorito = new Favorito(usuario, inmueble);
        favoritoDAO.save(favorito);

        List<Favorito> porUsuario = favoritoDAO.findByUsuario(usuario);
        List<Favorito> porUsuarioId = favoritoDAO.findByUsuarioId(usuario.getId());
        List<Favorito> porInmuebleId = favoritoDAO.findByInmuebleId(inmueble.getId());

        boolean existe = favoritoDAO.existsByUsuarioAndInmueble(usuario, inmueble);

        assertThat(porUsuario).hasSize(1);
        assertThat(porUsuarioId).hasSize(1);
        assertThat(porInmuebleId).hasSize(1);
        assertThat(existe).isTrue();

        favoritoDAO.deleteByUsuarioAndInmueble(usuario, inmueble);
        assertThat(favoritoDAO.findAll()).isEmpty();

        favoritoDAO.save(new Favorito(usuario, inmueble));
        favoritoDAO.deleteByInmuebleId(inmueble.getId());
        assertThat(favoritoDAO.findAll()).isEmpty();
    }
}

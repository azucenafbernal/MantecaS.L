package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mantecasl.accommodationapp.business.entity.Inquilino;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Propietario;

@DataJpaTest
class InquilinoDAOTest {

    @Autowired
    private InquilinoDAO inquilinoDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private PropietarioDAO propietarioDAO;

    private Inmueble crearInmuebleValido() {
        Usuario u = new Usuario();
        u.setNombre("Prop");
        u.setEmail("prop@test.com");
        u.setContrasena("1234");
        u = usuarioDAO.save(u);

        Propietario p = new Propietario();
        p.setUsuario(u);
        p = propietarioDAO.save(p);

        Inmueble inmueble = new Inmueble();
        inmueble.setCiudad("Madrid");
        inmueble.setCapacidad(2);
        inmueble.setPrecioNoche(80);
        inmueble.setPropietario(p);
        return inmuebleDAO.save(inmueble);
    }

    @Test
    void findByUsuarioId_devuelve_inquilino() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Inquilino");
        usuario.setEmail("inq@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        Inmueble inmueble = crearInmuebleValido();

        Inquilino inquilino = new Inquilino(usuario, "600000000", "DNI123", "TARJETA");
        inquilino.setInmueble(inmueble);
        inquilinoDAO.save(inquilino);

        Optional<Inquilino> resultado = inquilinoDAO.findByUsuarioId(usuario.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getDocumentoIdentidad()).isEqualTo("DNI123");
    }

    @Test
    void findByUsuario_devuelve_inquilino() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Inquilino2");
        usuario.setEmail("inq2@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        Inmueble inmueble = crearInmuebleValido();

        Inquilino inquilino = new Inquilino(usuario, "611111111", "DNI999", "PAYPAL");
        inquilino.setInmueble(inmueble);
        inquilinoDAO.save(inquilino);

        Optional<Inquilino> resultado = inquilinoDAO.findByUsuario(usuario);

        assertThat(resultado).isPresent();
    }

    @Test
    void findByDocumentoIdentidad_devuelve_inquilino() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Inquilino3");
        usuario.setEmail("inq3@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        Inmueble inmueble = crearInmuebleValido();

        Inquilino inquilino = new Inquilino(usuario, "622222222", "ABC123", "TARJETA");
        inquilino.setInmueble(inmueble);
        inquilinoDAO.save(inquilino);

        Optional<Inquilino> resultado = inquilinoDAO.findByDocumentoIdentidad("ABC123");

        assertThat(resultado).isPresent();
    }

    @Test
    void existsByUsuarioId_true_y_false() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Inquilino4");
        usuario.setEmail("inq4@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        Inmueble inmueble = crearInmuebleValido();

        Inquilino inquilino = new Inquilino(usuario, "633333333", "XYZ999", "PAYPAL");
        inquilino.setInmueble(inmueble);
        inquilinoDAO.save(inquilino);

        assertThat(inquilinoDAO.existsByUsuarioId(usuario.getId())).isTrue();
        assertThat(inquilinoDAO.existsByUsuarioId(999L)).isFalse();
    }
}

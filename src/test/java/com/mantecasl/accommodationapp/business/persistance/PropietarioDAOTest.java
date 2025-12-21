package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mantecasl.accommodationapp.business.entity.Propietario;
import com.mantecasl.accommodationapp.business.entity.Usuario;

@DataJpaTest
class PropietarioDAOTest {

    @Autowired
    private PropietarioDAO propietarioDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Test
    void findByUsuarioId_devuelve_propietario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Propietario");
        usuario.setEmail("prop@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        Propietario propietario = new Propietario();
        propietario.setUsuario(usuario);
        propietarioDAO.save(propietario);

        Propietario resultado = propietarioDAO.findByUsuarioId(usuario.getId());

        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsuario().getEmail()).isEqualTo("prop@test.com");
    }

    @Test
    void findByUsuarioEmail_devuelve_propietario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Propietario2");
        usuario.setEmail("prop2@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        Propietario propietario = new Propietario();
        propietario.setUsuario(usuario);
        propietarioDAO.save(propietario);

        Propietario resultado = propietarioDAO.findByUsuarioEmail("prop2@test.com");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    void findByUsuarioId_no_existe_devuelve_null() {
        Propietario resultado = propietarioDAO.findByUsuarioId(999L);
        assertThat(resultado).isNull();
    }

    @Test
    void findByUsuarioEmail_no_existe_devuelve_null() {
        Propietario resultado = propietarioDAO.findByUsuarioEmail("no@existe.com");
        assertThat(resultado).isNull();
    }
}

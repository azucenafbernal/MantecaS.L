package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mantecasl.accommodationapp.business.entity.Usuario;

@DataJpaTest
class UsuarioDAOTest {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Test
    void findByEmail_devuelve_usuario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Alejandro");
        usuario.setEmail("alejandro@test.com");
        usuario.setContrasena("1234");

        usuarioDAO.save(usuario);

        Usuario encontrado = usuarioDAO.findByEmail("alejandro@test.com");

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getEmail()).isEqualTo("alejandro@test.com");
    }

    @Test
    void findByEmail_no_existe_devuelve_null() {
        Usuario encontrado = usuarioDAO.findByEmail("noexiste@test.com");

        assertThat(encontrado).isNull();
    }
}

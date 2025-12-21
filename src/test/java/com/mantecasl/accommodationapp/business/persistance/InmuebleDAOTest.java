package com.mantecasl.accommodationapp.business.persistance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Propietario;
import com.mantecasl.accommodationapp.business.entity.Usuario;

@DataJpaTest
class InmuebleDAOTest {

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private PropietarioDAO propietarioDAO;

    @Test
    void findByPropietarioUsuarioId_devuelve_inmuebles() {
        // Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre("Propietario");
        usuario.setEmail("prop@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        // Propietario
        Propietario propietario = new Propietario();
        propietario.setUsuario(usuario);
        propietario = propietarioDAO.save(propietario);

        // Inmueble
        Inmueble inmueble = new Inmueble();
        inmueble.setCiudad("Madrid");
        inmueble.setCapacidad(3);
        inmueble.setPrecioNoche(100);
        inmueble.setPropietario(propietario);

        inmuebleDAO.save(inmueble);

        // Ejecución
        List<Inmueble> resultado = inmuebleDAO.findByPropietarioUsuarioId(usuario.getId());

        // Verificación
        assertThat(resultado)
            .isNotNull()
            .hasSize(1)
            .element(0)
            .extracting(Inmueble::getCiudad)
            .isEqualTo("Madrid");
    }

    @Test
    void findByPropietarioUsuarioId_sin_resultados() {
        List<Inmueble> resultado = inmuebleDAO.findByPropietarioUsuarioId(999L);

        assertThat(resultado)
            .isNotNull()
            .isEmpty();
    }
}

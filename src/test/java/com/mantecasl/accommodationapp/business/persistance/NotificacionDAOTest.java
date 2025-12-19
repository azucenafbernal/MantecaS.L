package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mantecasl.accommodationapp.business.entity.Notificacion;
import com.mantecasl.accommodationapp.business.entity.Usuario;

@DataJpaTest
class NotificacionDAOTest {

    @Autowired
    private NotificacionDAO notificacionDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    private Notificacion crearNotificacion(Usuario usuario, boolean leida) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setTitulo("Titulo");
        n.setMensaje("Mensaje");
        n.setTipo("INFO"); // 👈 OBLIGATORIO
        n.setLeida(leida);
        return n;
    }

    @Test
    void findByUsuarioOrderByFechaCreacionDesc() {
        Usuario usuario = new Usuario();
        usuario.setNombre("User");
        usuario.setEmail("user@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        notificacionDAO.save(crearNotificacion(usuario, false));
        notificacionDAO.save(crearNotificacion(usuario, true));

        List<Notificacion> resultado = notificacionDAO.findByUsuarioOrderByFechaCreacionDesc(usuario);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void findByUsuarioAndLeidaFalseOrderByFechaCreacionDesc() {
        Usuario usuario = new Usuario();
        usuario.setNombre("User2");
        usuario.setEmail("user2@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        notificacionDAO.save(crearNotificacion(usuario, false));
        notificacionDAO.save(crearNotificacion(usuario, true));

        List<Notificacion> resultado = notificacionDAO.findByUsuarioAndLeidaFalseOrderByFechaCreacionDesc(usuario);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).isLeida()).isFalse();
    }

    @Test
    void countByUsuarioAndLeidaFalse() {
        Usuario usuario = new Usuario();
        usuario.setNombre("User3");
        usuario.setEmail("user3@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        notificacionDAO.save(crearNotificacion(usuario, false));
        notificacionDAO.save(crearNotificacion(usuario, false));

        long contador = notificacionDAO.countByUsuarioAndLeidaFalse(usuario);

        assertThat(contador).isEqualTo(2);
    }

    @Test
    void marcarTodasComoLeidas_actualiza_registros() {
        Usuario usuario = new Usuario();
        usuario.setNombre("User4");
        usuario.setEmail("user4@test.com");
        usuario.setContrasena("1234");
        usuario = usuarioDAO.save(usuario);

        notificacionDAO.save(crearNotificacion(usuario, false));
        notificacionDAO.save(crearNotificacion(usuario, false));

        int actualizadas = notificacionDAO.marcarTodasComoLeidas(usuario);

        assertThat(actualizadas).isEqualTo(2);
        assertThat(notificacionDAO.countByUsuarioAndLeidaFalse(usuario)).isZero();
    }
}

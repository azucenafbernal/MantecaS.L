package com.mantecasl.accommodationapp.business.persistance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mantecasl.accommodationapp.business.entity.Notificacion;
import com.mantecasl.accommodationapp.business.entity.Usuario;

@Repository
public interface NotificacionDAO extends JpaRepository<Notificacion, Long> {
    
    // Encontrar notificaciones de un usuario
    List<Notificacion> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    
    // Encontrar notificaciones no leídas de un usuario
    List<Notificacion> findByUsuarioAndLeidaFalseOrderByFechaCreacionDesc(Usuario usuario);
    
    // Contar notificaciones no leídas
    long countByUsuarioAndLeidaFalse(Usuario usuario);

    void deleteByInmuebleId(Long inmuebleId);
    void deleteByReservaInmuebleId(Long inmuebleId);

    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario = :usuario AND n.leida = false")
    int marcarTodasComoLeidas(@Param("usuario") Usuario usuario);
}
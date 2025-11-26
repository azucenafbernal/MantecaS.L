package com.mantecasl.accommodationapp.business.persistance;

import com.mantecasl.accommodationapp.business.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface ReservaDAO extends JpaRepository<Reserva, Long>{
    List<Reserva> findByInmuebleId(Long inmuebleId);
    List<Reserva> findByInquilinoId(Long inquilinoId);
    List<Reserva> findByEstado(String estado);
    
    @Query("SELECT r FROM Reserva r WHERE r.inmueble.id = :inmuebleId AND r.estado IN ('CONFIRMADA', 'PENDIENTE') " +
           "AND r.fechaInicio <= :fechaFin AND r.fechaFin >= :fechaInicio")
    List<Reserva> findReservasActivasEnRango(
        @Param("inmuebleId") Long inmuebleId,
        @Param("fechaInicio") Date fechaInicio,
        @Param("fechaFin") Date fechaFin
    );
    
    @Query("SELECT r FROM Reserva r WHERE r.inquilino.usuario.id = :usuarioId")
    List<Reserva> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    java.util.List<Reserva> findByInmuebleIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Long inmuebleId, java.time.LocalDate fechaFin, java.time.LocalDate fechaInicio);

}

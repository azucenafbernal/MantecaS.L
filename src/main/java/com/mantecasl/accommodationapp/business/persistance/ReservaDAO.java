package com.mantecasl.accommodationapp.business.persistance;

import com.mantecasl.accommodationapp.business.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface ReservaDAO extends JpaRepository<Reserva, Long> {
    
    // Para verificar disponibilidad
    @Query("SELECT r FROM Reserva r WHERE r.inmueble.id = :inmuebleId " +
           "AND r.estado IN ('PENDIENTE', 'CONFIRMADA') " +
           "AND ((r.fechaInicio BETWEEN :inicio AND :fin) OR " +
           "(r.fechaFin BETWEEN :inicio AND :fin) OR " +
           "(r.fechaInicio <= :inicio AND r.fechaFin >= :fin))")
    List<Reserva> findReservasActivasEnRango(@Param("inmuebleId") Long inmuebleId, 
                                           @Param("inicio") Date inicio, 
                                           @Param("fin") Date fin);
    
    List<Reserva> findByInmuebleId(Long inmuebleId);
    List<Reserva> findByInquilinoUsuarioId(Long usuarioId);

    @Query("SELECT r FROM Reserva r WHERE r.inmueble.id = :inmuebleId AND r.estado = :estado")
    List<Reserva> findByInmuebleIdAndEstado(@Param("inmuebleId") Long inmuebleId, @Param("estado") String estado);
}

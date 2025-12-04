package com.mantecasl.accommodationapp.business.persistance;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mantecasl.accommodationapp.business.entity.Reserva;

@Repository
public interface ReservaDAO extends JpaRepository<Reserva, Long> {
    
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
        
    @Query("SELECT r FROM Reserva r WHERE r.inmueble.id = :inmuebleId AND r.fechaInicio >= CURRENT_DATE")
    List<Reserva> findReservasFuturasByInmueble(@Param("inmuebleId") Long inmuebleId);
    
    @Query("SELECT r FROM Reserva r WHERE r.inmueble.id = :inmuebleId " +
           "AND r.fechaInicio >= CURRENT_DATE " +
           "AND r.estado = :estado")
    List<Reserva> findReservasFuturasByInmuebleAndEstado(@Param("inmuebleId") Long inmuebleId, 
                                                         @Param("estado") String estado);
    
    default List<Reserva> findReservasConfirmadasFuturasByInmueble(Long inmuebleId) {
        return findReservasFuturasByInmuebleAndEstado(inmuebleId, "CONFIRMADA");
    }
    
    @Query("SELECT r FROM Reserva r WHERE r.inmueble.id = :inmuebleId " +
           "AND r.fechaInicio >= :hoy " +
           "AND r.estado = 'CONFIRMADA'")
    List<Reserva> findReservasConfirmadasFuturasDesdeFecha(@Param("inmuebleId") Long inmuebleId,
                                                           @Param("hoy") Date hoy);
    
    @Modifying
    @Query("DELETE FROM Reserva r WHERE r.inmueble.id = :inmuebleId")
    void deleteByInmuebleId(@Param("inmuebleId") Long inmuebleId);
}

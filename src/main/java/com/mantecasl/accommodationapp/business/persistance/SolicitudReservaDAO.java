package com.mantecasl.accommodationapp.business.persistance;

import com.mantecasl.accommodationapp.business.entity.SolicitudReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudReservaDAO extends JpaRepository<SolicitudReserva, Long> {
     // Encuentra solicitudes por estado
    List<SolicitudReserva> findByEstado(String estado);
    
    // Encuentra solicitudes por inmueble
    List<SolicitudReserva> findByInmuebleId(Long inmuebleId);
    
    // Encuentra solicitudes por inmueble y estado
    List<SolicitudReserva> findByInmuebleIdAndEstado(Long inmuebleId, String estado);

    
    // Encuentra solicitudes por propietario
    @Query("SELECT s FROM SolicitudReserva s WHERE s.inmueble.propietario.id = :propietarioId")
    List<SolicitudReserva> findByPropietarioId(@Param("propietarioId") Long propietarioId);
    
    // Encuentra solicitudes pendientes por propietario
    @Query("SELECT s FROM SolicitudReserva s WHERE s.inmueble.propietario.id = :propietarioId AND s.estado = 'PENDIENTE'")
    List<SolicitudReserva> findPendientesByPropietarioId(@Param("propietarioId") Long propietarioId);

    List<SolicitudReserva> findByInmueblePropietarioId(Long propietarioId);

    List<SolicitudReserva> findByInmueble_PropietarioId(Long propietarioId);

    List<SolicitudReserva> findByInmueblePropietarioIdAndEstado(Long propietarioId, String estado);
    
    List<SolicitudReserva> findByInmueble_PropietarioIdAndEstado(Long propietarioId, String estado);

    @Query("SELECT s FROM SolicitudReserva s WHERE s.inmueble.id = :inmuebleId AND s.estado = 'PENDIENTE'")
    List<SolicitudReserva> findPendientesByInmuebleId(@Param("inmuebleId") Long inmuebleId);

    long countByInmueblePropietarioIdAndEstado(Long propietarioId, String estado);
    
    List<SolicitudReserva> findByInquilinoId(Long inquilinoId);
    
    void deleteByInmuebleId(Long inmuebleId);

    @Modifying
    @Query("DELETE FROM SolicitudReserva s WHERE s.reserva.id = :reservaId")
    void deleteByReservaId(@Param("reservaId") Long reservaId);

    // O si no funciona con JPQL, usa nativo:
    @Modifying
    @Query(value = "DELETE FROM solicitudes_reserva WHERE reserva_id = :reservaId", nativeQuery = true)
    void deleteByReservaIdNative(@Param("reservaId") Long reservaId);
}
package com.mantecasl.accommodationapp.business.persistance;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.mantecasl.accommodationapp.business.entity.Disponibilidad;

@Repository
public interface DisponibilidadDAO extends JpaRepository<Disponibilidad, Long> {
    List<Disponibilidad> findByInmuebleId(Long inmuebleId);
    //Para disponibilidades activas
    List<Disponibilidad> findByInmuebleIdAndDisponibleTrue(Long inmuebleId);
    //Para disponibilidades bloqueadas
    List<Disponibilidad> findByInmuebleIdAndDisponibleFalse(Long inmuebleId);

    @Query("SELECT d FROM Disponibilidad d WHERE " +"d.inmueble.id = :inmuebleId AND " +
           "d.disponible = true AND " +"d.fechaInicio <= :fechaFin AND " +
           "d.fechaFin >= :fechaInicio")

    //Para busquedas por fechas
    List<Disponibilidad> findDisponiblesEnRango(
        @Param("inmuebleId") Long inmuebleId,
        @Param("fechaInicio") Date fechaInicio,
        @Param("fechaFin") Date fechaFin
    );

    void deleteByInmuebleId(Long inmuebleId);
}

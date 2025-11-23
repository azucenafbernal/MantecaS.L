package com.mantecasl.accommodationapp.business.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mantecasl.accommodationapp.business.entity.Reserva;
import java.util.List;

@Repository
public interface ReservaDAO extends JpaRepository<Reserva, Long>{
    java.util.List<Reserva> findByInmuebleIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Long inmuebleId, java.time.LocalDate fechaFin, java.time.LocalDate fechaInicio);
            List<Reserva> findByInmuebleId(Long inmuebleId);

}

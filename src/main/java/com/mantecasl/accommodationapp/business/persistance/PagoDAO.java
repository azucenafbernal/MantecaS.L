package com.mantecasl.accommodationapp.business.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mantecasl.accommodationapp.business.entity.Pago;

@Repository
public interface PagoDAO extends JpaRepository<Pago, Long>{

}

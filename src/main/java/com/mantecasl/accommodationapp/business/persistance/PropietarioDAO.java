package com.mantecasl.accommodationapp.business.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mantecasl.accommodationapp.business.entity.Propietario;

@Repository
public interface PropietarioDAO extends JpaRepository<Propietario, Long> {
    //Método para buscar propietario por ID de usuario
    Propietario findByUsuarioId(Long usuarioId);
    
    //Método alternativo por email
    Propietario findByUsuarioEmail(String email);
}

package com.mantecasl.accommodationapp.business.persistance;

import com.mantecasl.accommodationapp.business.entity.Inquilino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquilinoDAO extends JpaRepository<Inquilino, Long> {
    Optional<Inquilino> findByUsuarioId(Long usuarioId);
    Optional<Inquilino> findByDocumentoIdentidad(String documentoIdentidad);
    boolean existsByUsuarioId(Long usuarioId);
}
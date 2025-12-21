package com.mantecasl.accommodationapp.business.persistance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mantecasl.accommodationapp.business.entity.Inquilino;
import com.mantecasl.accommodationapp.business.entity.Usuario;

@Repository
public interface InquilinoDAO extends JpaRepository<Inquilino, Long> {
    Optional<Inquilino> findByUsuarioId(Long usuarioId);

    Optional<Inquilino> findByUsuario(Usuario usuario);

    Optional<Inquilino> findByDocumentoIdentidad(String documentoIdentidad);
    
    boolean existsByUsuarioId(Long usuarioId);

    List<Inquilino> findByInmuebleId(Long inmuebleId);
}
package com.mantecasl.accommodationapp.business.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mantecasl.accommodationapp.business.entity.Favorito;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.entity.Inmueble;

import java.util.List;

@Repository
public interface FavoritoDAO extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuario(Usuario usuario);
    
    List<Favorito> findByInmuebleId(Long inmuebleId);

    boolean existsByUsuarioAndInmueble(Usuario usuario, Inmueble inmueble);

    void deleteByUsuarioAndInmueble(Usuario usuario, Inmueble inmueble);

    @Modifying
    @Query("DELETE FROM Favorito f WHERE f.inmueble.id = :inmuebleId")
    void deleteByInmuebleId(@Param("inmuebleId") Long inmuebleId);
}

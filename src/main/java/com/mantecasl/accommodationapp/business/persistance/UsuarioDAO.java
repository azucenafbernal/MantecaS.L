package com.mantecasl.accommodationapp.business.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mantecasl.accommodationapp.business.entity.Usuario;

@Repository
public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
    // Busca un usuario por su email
    Usuario findByEmail(String email);
    
}

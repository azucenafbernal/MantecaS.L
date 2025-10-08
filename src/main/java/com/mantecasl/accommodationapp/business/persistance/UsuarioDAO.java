package com.mantecasl.accommodationapp.business.persistance;

import java.util.ArrayList;
import java.util.List;

import com.mantecasl.accommodationapp.business.entity.Usuario;

public class UsuarioDAO {
    private List<Usuario> usuarios = new ArrayList<>();

    public boolean agregarUsuario(Usuario usuario) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getEmail().equals(usuario.getEmail())) {
                return false; // Usuario ya existe
            }
        }
        usuarios.add(usuario);
        return true;
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getEmail().equals(email)) {
                return usuarios.get(i);
            }
        }
        return null; // Usuario no encontrado
    }

}


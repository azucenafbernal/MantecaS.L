package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;

public class GestorUsuarios {
    private UsuarioDAO usuarioDAO  = new UsuarioDAO();

    public String registrarUsuario(String nombre, String email, String contrasena) {
        Usuario nuevoUsuario = new Usuario(nombre, email, contrasena);
        boolean exito = usuarioDAO.agregarUsuario(nuevoUsuario);
        if (exito) {
            return "Usuario registrado exitosamente.";
        } else {
            return "Error: El usuario con este email ya existe.";
        }
    }
}

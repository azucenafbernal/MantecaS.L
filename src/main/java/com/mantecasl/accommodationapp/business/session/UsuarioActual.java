package com.mantecasl.accommodationapp.business.session;

import com.mantecasl.accommodationapp.business.entity.Usuario;

public class UsuarioActual {
    public static Usuario usuarioLogueado = null;

    public static void cerrarSesion() {
        usuarioLogueado = null;
    }
}

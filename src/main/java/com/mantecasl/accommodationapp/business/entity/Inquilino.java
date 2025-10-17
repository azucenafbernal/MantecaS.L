package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inquilino")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Inquilino extends Usuario {

    public Inquilino() {}

    public Inquilino(String nombre, String email, String contrasena) {
        super(nombre, email, contrasena);
    }
}

package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "propietario")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Propietario extends Usuario {

    private String telefonoContacto;
    private String cuentaBancaria;

    public Propietario() {}

    public Propietario(String nombre, String email, String contrasena, String telefonoContacto, String cuentaBancaria) {
        super(nombre, email, contrasena);
        this.telefonoContacto = telefonoContacto;
        this.cuentaBancaria = cuentaBancaria;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(String cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }
}

package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;

@Entity
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Propietario extends Usuario {

    @OneToOne
    @JoinColumn(name = "inmueble_id")
    private Inmueble inmueble;

    private String telefonoContacto;
    private String cuentaBancaria;

    public Propietario() {}

    public Propietario(Inmueble inmueble, String telefonoContacto, String cuentaBancaria) {
        this.inmueble= inmueble;
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

    public Inmueble getInmueble() {
        return inmueble;
    }

    public void setInmueble(Inmueble inmueble) {
        this.inmueble = inmueble;
    }
}

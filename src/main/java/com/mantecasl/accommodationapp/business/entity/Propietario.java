package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propietario")
public class Propietario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    //Relación OneToOne (1:1) con Usuario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;
    
    private String telefonoContacto;
    private String cuentaBancaria;
    
    //Relación OneToMany con Inmuebles (Un propietario puede tener varios Inmuebles)
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inmueble> inmuebles = new ArrayList<>();

    //Constructores
    public Propietario() {}

    public Propietario(Usuario usuario, String telefonoContacto, String cuentaBancaria) {
        this.usuario = usuario;
        this.telefonoContacto = telefonoContacto;
        this.cuentaBancaria = cuentaBancaria;
    }

    //Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    public List<Inmueble> getInmuebles() {
        return inmuebles;
    }

    public void setInmuebles(List<Inmueble> inmuebles) {
        this.inmuebles = inmuebles;
    }

    //Método auxiliar para agregar inmueble
    public void agregarInmueble(Inmueble inmueble) {
        inmuebles.add(inmueble);
        inmueble.setPropietario(this);
    }
    
    //Método para obtener el email del usuario (útil para la interfaz)
    public String getEmailUsuario() {
        return usuario != null ? usuario.getEmail() : null;
    }
    
    //Método para obtener el nombre del usuario (útil para la interfaz)
    public String getNombreUsuario() {
        return usuario != null ? usuario.getNombre() : null;
    }
}
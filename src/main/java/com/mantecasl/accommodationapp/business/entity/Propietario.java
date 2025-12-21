package com.mantecasl.accommodationapp.business.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
    private String numeroTarjeta;
    private String fechaVencimiento;
    private String cvv;
    
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

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
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
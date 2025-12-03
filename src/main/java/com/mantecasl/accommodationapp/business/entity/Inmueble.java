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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "inmueble")
public class Inmueble {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String calle;
    private String numero;
    private String ciudad;
    private String codigoPostal;
    private double precioNoche;
    private String descripcion;
    private Integer capacidad;

    private boolean reservaDirecta = true; 
    
    //Relación con Propietario (Un propietario puede tener asociados muchos inmuebles)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Propietario propietario;
    
    @OneToMany(mappedBy = "inmueble", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();
    
    @OneToMany(mappedBy = "inmueble", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilidad> disponibilidades = new ArrayList<>();
    
    @OneToMany(mappedBy = "inmueble", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorito> favoritos = new ArrayList<>();
    
    //Constructores
    public Inmueble() {}
    
    public Inmueble(String calle, String numero, String ciudad, String codigoPostal, double precioNoche, String descripcion, Integer capacidad, boolean reservaDirecta, Propietario propietario) {
        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
        this.precioNoche = precioNoche;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
        this.propietario = propietario;
    }
    
    //Constructor alternativo para compatibilidad
    public Inmueble(String calle, String numero, String ciudad, String codigoPostal, double precioNoche, String descripcion, Integer capacidad, Usuario usuario) {
        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
        this.precioNoche = precioNoche;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
        //Nota: En este constructor no establecemos propietario
        //Se establecerá cuando se cree el Propietario
    }

    //Getters y Setters
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCalle() { 
        return calle; 
    }
    public void setCalle(String calle) { 
        this.calle = calle; 
    }
    public String getNumero() { 
        return numero; 
    }
    public void setNumero(String numero) { 
        this.numero = numero; 
    }
    public String getCiudad() { 
        return ciudad; 
    }
    public void setCiudad(String ciudad) { 
        this.ciudad = ciudad; 
    }
    public String getCodigoPostal() { 
        return codigoPostal; 
    }
    public void setCodigoPostal(String codigoPostal) { 
        this.codigoPostal = codigoPostal; 
    }
    public double getPrecioNoche() { 
        return precioNoche; 
    }
    public void setPrecioNoche(double precioNoche) {
         this.precioNoche = precioNoche; 
    }
    public boolean isReservaDirecta() {
        return reservaDirecta;
    }
    public void setReservaDirecta(boolean reservaDirecta) {
        this.reservaDirecta = reservaDirecta;
    }
    public String getDescripcion() { 
        return descripcion; 
    }
    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion; 
    }
    public Integer getCapacidad() { 
        return capacidad; 
    }
    public void setCapacidad(Integer capacidad) { 
        this.capacidad = capacidad; 
    }
    public Propietario getPropietario() { 
        return propietario; 
    }
    public void setPropietario(Propietario propietario) { 
        this.propietario = propietario;
    }

    public List<Favorito> getFavoritos() {
        return favoritos;
    }
    public void setFavoritos(List<Favorito> favoritos) {
        this.favoritos = favoritos;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }
    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    public List<Disponibilidad> getDisponibilidades() {
        return disponibilidades;
    }
    public void setDisponibilidades(List<Disponibilidad> disponibilidades) {
        this.disponibilidades = disponibilidades;
    }

    //Metodo para obtener el usuario del propietario
    public Usuario getUsuario() {
        return propietario != null ? propietario.getUsuario() : null;
    }
    
    public String getDireccion() {
        return calle + " " + numero + ", " + ciudad + " " + codigoPostal;
    }
}


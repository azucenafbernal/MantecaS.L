package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inmueble")
public class Inmueble {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String direccion;
    private double precioNoche;
    private String descripcion;
    private Integer capacidad;
    
    //Relación con Propietario (Un propietario puede tener asociados muchos inmuebles)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Propietario propietario;
    
    //Constructores
    public Inmueble() {}
    
    public Inmueble(String direccion, double precioNoche, String descripcion, Integer capacidad, Propietario propietario) {
        this.direccion = direccion;
        this.precioNoche = precioNoche;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
        this.propietario = propietario;
    }
    
    //Constructor alternativo para compatibilidad
    public Inmueble(String direccion, double precioNoche, String descripcion, Integer capacidad, Usuario usuario) {
        this.direccion = direccion;
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
    public String getDireccion() { 
        return direccion; 
    }
    public void setDireccion(String direccion) { 
        this.direccion = direccion; 
    }
    public double getPrecioNoche() { 
        return precioNoche; 
    }
    public void setPrecioNoche(double precioNoche) {
         this.precioNoche = precioNoche; 
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

    //Método para obtener el usuario del propietario
    public Usuario getUsuario() {
        return propietario != null ? propietario.getUsuario() : null;
    }
}
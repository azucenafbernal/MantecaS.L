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
    
    //Solo relación con Usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id") 
    private Usuario usuario;
    
    //Constructores
    public Inmueble() {}
    
    public Inmueble(String direccion, double precioNoche, String descripcion, Integer capacidad, Usuario usuario) {
        this.direccion = direccion;
        this.precioNoche = precioNoche;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getId() { 
        return id; 
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
    public Usuario getUsuario() { 
        return usuario; 
    }
    public void setUsuario(Usuario usuario) { 
        this.usuario = usuario;
    }
}
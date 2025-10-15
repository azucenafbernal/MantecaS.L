package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;

@Entity
public class Inmueble {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String direccion;
    private double precioNoche;
    
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
}

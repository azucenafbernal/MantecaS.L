package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String contrasena;
    
    private String telefonoContacto;
    private String cuentaBancaria;
    
    // Relación con inmuebles
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inmueble> inmuebles = new ArrayList<>();

    public Usuario() {}

    public Usuario(String nombre, String email, String contrasena) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
    }

    //Getters y Setters
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }
    
    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getContrasena() { 
        return contrasena; 
    }
    public void setContrasena(String contrasena) { 
        this.contrasena = contrasena; 
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
    
    //Método para verificar si es propietario
    public boolean esPropietario() {
        return this.telefonoContacto != null && this.cuentaBancaria != null;
    }
}
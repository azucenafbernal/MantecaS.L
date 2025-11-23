package com.mantecasl.accommodationapp.business.entity;

import java.sql.Date;

import jakarta.persistence.*;

@Entity
@Table(name="disponibilidades")
public class Disponibilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="inmueble_id")
    private Inmueble inmueble;

    private Date fechaInicio;
    private Date fechaFin;
    private double precio;
    private boolean directa;
    private boolean disponible;


    //Constructores
    public Disponibilidad(){
        this.disponible = true;
    }

    public Disponibilidad(Inmueble inmueble, Date fechaInicio, Date fechaFin, double precio, boolean directa){
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precio = precio;
        this.directa = directa;
        this.disponible = true;
    }

    //Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Inmueble getInmueble() {
        return inmueble;
    }

    public void setInmueble(Inmueble inmueble) {
        this.inmueble = inmueble;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isDirecta() {
        return directa;
    }

    public void setDirecta(boolean directa) {
        this.directa = directa;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean esValida(Date fecha){
        return !fecha.before(fechaInicio) && !fecha.after(fechaFin);
    }
}

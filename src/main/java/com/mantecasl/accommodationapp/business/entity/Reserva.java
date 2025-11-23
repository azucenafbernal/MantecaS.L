package com.mantecasl.accommodationapp.business.entity;

import java.sql.Date;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    @JoinColumn(name = "inmueble_id")

    @ManyToOne
    private Inmueble inmueble;

    public Reserva() {
    }

    public Reserva(Inmueble inmueble, LocalDate fechaInicio, LocalDate fechaFin) {
        this.inmueble = inmueble;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
    
    public Long getId() {
        return id;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Inmueble getInmueble() {
        return inmueble;
    }
    public void setInmueble(Inmueble inmueble) {
        this.inmueble = inmueble;
    }

}

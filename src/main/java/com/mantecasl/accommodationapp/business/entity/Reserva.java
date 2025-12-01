package com.mantecasl.accommodationapp.business.entity;

import java.sql.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "reserva")
    private SolicitudReserva solicitudOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inmueble_id", nullable = false)
    private Inmueble inmueble;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquilino_id", nullable = false)
    private Inquilino inquilino;

    @Column(nullable = false)
    private Date fechaInicio;

    @Column(nullable = false)
    private Date fechaFin;

    @Column(nullable = false)
    private double precioTotal;

    @Column(nullable = false)
    private String estado; // "CONFIRMADA", "CANCELADA", "COMPLETADA"

    private String metodoPagoUsado;

    // Constructores
    public Reserva() {
        this.estado = "PENDIENTE";
    }

    // Constructor para reservas directas
    public Reserva(Inmueble inmueble, Inquilino inquilino, Date fechaInicio, 
                  Date fechaFin, double precioTotal) {
        this();
        this.inmueble = inmueble;
        this.inquilino = inquilino;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precioTotal = precioTotal;
        this.estado = "CONFIRMADA";
        this.metodoPagoUsado = inquilino.getMetodoPago();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SolicitudReserva getSolicitudOrigen() { return solicitudOrigen; }
    public void setSolicitudOrigen(SolicitudReserva solicitudOrigen) { this.solicitudOrigen = solicitudOrigen; }

    public Inmueble getInmueble() { return inmueble; }
    public void setInmueble(Inmueble inmueble) { this.inmueble = inmueble; }

    public Inquilino getInquilino() { return inquilino; }
    public void setInquilino(Inquilino inquilino) { this.inquilino = inquilino; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    public double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(double precioTotal) { this.precioTotal = precioTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMetodoPagoUsado() { return metodoPagoUsado; }
    public void setMetodoPagoUsado(String metodoPagoUsado) { this.metodoPagoUsado = metodoPagoUsado; }

    // Métodos de negocio
    public void confirmar() {
        this.estado = "CONFIRMADA";
        this.metodoPagoUsado = this.inquilino.getMetodoPago();
    }

    public void cancelar(String motivo) {
        this.estado = "CANCELADA";
    }

    public boolean estaActiva() {
        return "CONFIRMADA".equals(estado);
    }

    public long getNumeroNoches() {
        return (fechaFin.getTime() - fechaInicio.getTime()) / (1000 * 60 * 60 * 24);
    }
    
    @Transient
    public String getNumeroTarjeta() {
        return inquilino != null ? inquilino.getNumeroTarjeta() : null;
    }
    
    @Transient
    public String getPaypalEmail() {
        return inquilino != null ? inquilino.getPaypalEmail() : null;
    }
    
    @Transient
    public String getMetodoPago() {
        return metodoPagoUsado != null ? metodoPagoUsado : 
               (inquilino != null ? inquilino.getMetodoPago() : null);
    }
}
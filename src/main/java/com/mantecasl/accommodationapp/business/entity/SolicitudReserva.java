package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;
import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_reserva")
public class SolicitudReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquilino_id", nullable = false)
    private Inquilino inquilino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inmueble_id", nullable = false)
    private Inmueble inmueble;

    @Column(nullable = false)
    private Date fechaInicio;

    @Column(nullable = false)
    private Date fechaFin;

    @Column(nullable = false)
    private double precioTotal;

    @Column(nullable = false, length = 20)
    private String estado; // PENDIENTE, APROBADA, RECHAZADA

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(length = 500)
    private String observacionesInquilino;

    @Column(length = 500)
    private String mensajePropietario;

    @Column(length = 500)
    private String motivoRechazo;

    private LocalDateTime fechaDecision;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reserva_id", referencedColumnName = "id")
    private Reserva reserva;

    @Transient
    public long getNumeroNoches() {
        long diff = fechaFin.getTime() - fechaInicio.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    public SolicitudReserva() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public SolicitudReserva(Inquilino inquilino, Inmueble inmueble, Date fechaInicio, Date fechaFin, double precioTotal) {
        this();
        this.inquilino = inquilino;
        this.inmueble = inmueble;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precioTotal = precioTotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Inquilino getInquilino() { return inquilino; }
    public void setInquilino(Inquilino inquilino) { this.inquilino = inquilino; }

    public Inmueble getInmueble() { return inmueble; }
    public void setInmueble(Inmueble inmueble) { this.inmueble = inmueble; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    public double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(double precioTotal) { this.precioTotal = precioTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getObservacionesInquilino() { return observacionesInquilino; }
    public void setObservacionesInquilino(String observacionesInquilino) { this.observacionesInquilino = observacionesInquilino; }
    
    public String getObservaciones() { return observacionesInquilino; }
    public void setObservaciones(String observaciones) { this.observacionesInquilino = observaciones; }

    public String getMensajePropietario() { return mensajePropietario; }
    public void setMensajePropietario(String mensajePropietario) { this.mensajePropietario = mensajePropietario; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
    
    public String getMotivoCancelacion() { return motivoRechazo; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoRechazo = motivoCancelacion; }

    public LocalDateTime getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(LocalDateTime fechaDecision) { this.fechaDecision = fechaDecision; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }

    public void aprobar(String mensaje) {
        this.estado = "APROBADA";
        this.mensajePropietario = mensaje;
        this.fechaDecision = LocalDateTime.now();
    }

    public void rechazar(String motivo) {
        this.estado = "RECHAZADA";
        this.motivoRechazo = motivo;
        this.fechaDecision = LocalDateTime.now();
    }
    
    public void cancelar(String motivo) {
        rechazar(motivo);
    }
    public Reserva crearReserva() {
        if (!"APROBADA".equals(estado)) {
            throw new IllegalStateException("Solo se puede crear reserva de una solicitud aprobada");
        }
    
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setInmueble(this.inmueble);
        nuevaReserva.setInquilino(this.inquilino);
        nuevaReserva.setFechaInicio(this.fechaInicio);
        nuevaReserva.setFechaFin(this.fechaFin);
        nuevaReserva.setPrecioTotal(this.precioTotal);
        nuevaReserva.confirmar();
        
        this.reserva = nuevaReserva;
        return nuevaReserva;
    }
}
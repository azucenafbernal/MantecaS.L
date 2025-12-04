package com.mantecasl.accommodationapp.business.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificaciones")
public class Notificacion {
    
    public static final String SOLICITUD_NUEVA = "SOLICITUD_NUEVA";
    public static final String SOLICITUD_APROBADA = "SOLICITUD_APROBADA";
    public static final String SOLICITUD_RECHAZADA = "SOLICITUD_RECHAZADA";
    public static final String RESERVA_CONFIRMADA = "RESERVA_CONFIRMADA";
    public static final String RESERVA_CANCELADA = "RESERVA_CANCELADA";
    public static final String PROPIEDAD_ELIMINADA = "PROPIEDAD_ELIMINADA";
    public static final String PAGO_DEVUELTO = "PAGO_DEVUELTO";
    public static final String PAGO_REALIZADO = "PAGO_REALIZADO";
    public static final String AVISO_SISTEMA = "AVISO_SISTEMA";
    public static final String MENSAJE_PROPIETARIO = "MENSAJE_PROPIETARIO";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(nullable = false, length = 255)
    private String titulo;
    
    @Column(columnDefinition = "CLOB")
    private String mensaje;
    
    // Cambiar de Enum a String
    @Column(nullable = false, length = 50)
    private String tipo;
    
    @Column(nullable = false)
    private boolean leida = false;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inmueble_id")
    private Inmueble inmueble;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id")
    private SolicitudReserva solicitud;
    
    @Column
    private String accionUrl;
    
    // Constructores
    public Notificacion() {
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public Notificacion(Usuario usuario, String titulo, String mensaje, String tipo) {
        this();
        this.usuario = usuario;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
    }
    
    // Getters y Setters (actualizar los tipos)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public Inmueble getInmueble() { return inmueble; }
    public void setInmueble(Inmueble inmueble) { this.inmueble = inmueble; }
    
    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
    
    public SolicitudReserva getSolicitud() { return solicitud; }
    public void setSolicitud(SolicitudReserva solicitud) { this.solicitud = solicitud; }
    
    public String getAccionUrl() { return accionUrl; }
    public void setAccionUrl(String accionUrl) { this.accionUrl = accionUrl; }
    
    // Métodos de utilidad
    public String getFechaFormateada() {
        return fechaCreacion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public boolean isReciente() {
        return fechaCreacion.isAfter(LocalDateTime.now().minusHours(24));
    }
}
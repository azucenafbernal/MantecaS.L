package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inquilino")
public class Inquilino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Relación con Usuario
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    //Relación con Inmueble
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inmueble_id", nullable = false)
    private Inmueble inmueble;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String documentoIdentidad;

    @Column(nullable = false)
    private String metodoPago;

    // Datos de pago (opcionales, dependiendo del método)
    private String numeroTarjeta;
    private String fechaCaducidad;
    private String cvv;
    private String paypalEmail;

    public Inquilino() {}

    public Inquilino(Usuario usuario, String telefono, String documentoIdentidad, String metodoPago) {
        this.usuario = usuario;
        this.telefono = telefono;
        this.documentoIdentidad = documentoIdentidad;
        this.metodoPago = metodoPago;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Inmueble getInmueble() { return inmueble; }
    public void setInmueble(Inmueble inmueble) { this.inmueble = inmueble; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    public String getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(String fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getPaypalEmail() { return paypalEmail; }
    public void setPaypalEmail(String paypalEmail) { this.paypalEmail = paypalEmail; }

    // Método para actualizar datos de pago según el método seleccionado
    public void actualizarDatosPago(String metodoPago, String numeroTarjeta, 
                                   String fechaCaducidad, String cvv, String paypalEmail) {
        this.metodoPago = metodoPago;
        
        // Limpiar datos anteriores
        this.numeroTarjeta = null;
        this.fechaCaducidad = null;
        this.cvv = null;
        this.paypalEmail = null;
        
        // Establecer nuevos datos según el método
        if ("TARJETA".equals(metodoPago)) {
            this.numeroTarjeta = numeroTarjeta;
            this.fechaCaducidad = fechaCaducidad;
            this.cvv = cvv;
        } else if ("PAYPAL".equals(metodoPago)) {
            this.paypalEmail = paypalEmail;
        }
    }

    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombre() : "";
    }

    public String getEmail() {
        return usuario != null ? usuario.getEmail() : "";
    }
}
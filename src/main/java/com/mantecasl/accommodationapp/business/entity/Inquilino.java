package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inquilinos")
public class Inquilino {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String documentoIdentidad;

    private String preferencias;
    private Integer valoracionPromedio;

    @OneToMany(mappedBy = "inquilino", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();

    // Constructores
    public Inquilino() {}

    public Inquilino(Usuario usuario, String telefono, String documentoIdentidad) {
        this.usuario = usuario;
        this.telefono = telefono;
        this.documentoIdentidad = documentoIdentidad;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getPreferencias() {
        return preferencias;
    }

    public void setPreferencias(String preferencias) {
        this.preferencias = preferencias;
    }

    public Integer getValoracionPromedio() {
        return valoracionPromedio;
    }

    public void setValoracionPromedio(Integer valoracionPromedio) {
        this.valoracionPromedio = valoracionPromedio;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    // Métodos de negocio
    public void agregarReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setInquilino(this);
    }

    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombre() : "";
    }

    public String getEmail() {
        return usuario != null ? usuario.getEmail() : "";
    }
}
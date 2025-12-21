package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "favorito")
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "inmueble_id")
    private Inmueble inmueble;

    public Favorito() {
    }

    public Favorito(Usuario usuario, Inmueble inmueble) {
        this.usuario = usuario;
        this.inmueble = inmueble;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Inmueble getInmueble() {
        return inmueble;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setInmueble(Inmueble inmueble) {
        this.inmueble = inmueble;
    }
}
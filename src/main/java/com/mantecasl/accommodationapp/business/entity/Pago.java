package com.mantecasl.accommodationapp.business.entity;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID referencia;

    public Long getId() {
        return id;
    }
    
    public UUID getReferencia() {
        return referencia;
    }

    
}

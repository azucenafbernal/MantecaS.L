package com.mantecasl.accommodationapp.business.entity;

import jakarta.persistence.*;

@Entity
public class Inquilino extends Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }
    
}

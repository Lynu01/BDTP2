package com.example.vacaciones.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Usuario {
    @Id
    private Long id;
    private String username;
    private String password;

    // Getters y setters
}
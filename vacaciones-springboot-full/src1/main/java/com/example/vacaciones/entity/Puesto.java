package com.example.vacaciones.entity;

import jakarta.persistence.*;

@Entity
public class Puesto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Double salarioxHora;

    // Getters y setters
}
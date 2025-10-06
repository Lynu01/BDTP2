package com.example.demo.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

@Entity
public class Puesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double salarioxHora;

    // Constructores
    public Puesto() {
    }

    public Puesto(String nombre, Double salarioxHora) {
        this.nombre = nombre;
        this.salarioxHora = salarioxHora;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getSalarioxHora() {
        return salarioxHora;
    }

    public void setSalarioxHora(Double salarioxHora) {
        this.salarioxHora = salarioxHora;
    }
}

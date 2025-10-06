package com.example.demo.entidades;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 128)
    private String Nombre;

    @Column(nullable = false)
    private BigDecimal Salario;

    public void setSalario(BigDecimal Salario) {
        this.Salario = Salario;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String Nombre) {
        this.Nombre = Nombre;
    }

    public BigDecimal getSalario() {
        return Salario;
    }
    
}
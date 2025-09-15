package com.example.vacaciones.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Empleado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Puesto puesto;

    @Column(unique = true)
    private String valorDocumentoIdentidad;

    private String nombre;
    private LocalDate fechaContratacion;
    private Double saldoVacaciones;
    private Boolean esActivo;

    // Getters y setters
}
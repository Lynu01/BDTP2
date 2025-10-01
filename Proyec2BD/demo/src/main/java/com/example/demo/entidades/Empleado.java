package com.example.demo.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Empleado") 
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id") 
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "IdPuesto", referencedColumnName = "id") 
    private Puesto puesto;

    @Column(name = "ValorDocumentoIdentidad", nullable = false, unique = true) 
    private String valorDocumentoIdentidad;

    @Column(name = "Nombre", nullable = false) 
    private String nombre;

    @Column(name = "FechaContratacion", nullable = false) 
    private LocalDate fechaContratacion;

    @Column(name = "SaldoVacaciones", nullable = false) 
    private Double saldoVacaciones;

    @Column(name = "EsActivo", nullable = false) 
    private Boolean esActivo;

    // Constructores
    public Empleado() {
    }

    public Empleado(Puesto puesto, String valorDocumentoIdentidad, String nombre,
                    LocalDate fechaContratacion, Double saldoVacaciones, Boolean esActivo) {
        this.puesto = puesto;
        this.valorDocumentoIdentidad = valorDocumentoIdentidad;
        this.nombre = nombre;
        this.fechaContratacion = fechaContratacion;
        this.saldoVacaciones = saldoVacaciones;
        this.esActivo = esActivo;
    }

  
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public void setPuesto(Puesto puesto) {
        this.puesto = puesto;
    }

    public String getValorDocumentoIdentidad() {
        return valorDocumentoIdentidad;
    }

    public void setValorDocumentoIdentidad(String valorDocumentoIdentidad) {
        this.valorDocumentoIdentidad = valorDocumentoIdentidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public Double getSaldoVacaciones() {
        return saldoVacaciones;
    }

    public void setSaldoVacaciones(Double saldoVacaciones) {
        this.saldoVacaciones = saldoVacaciones;
    }

    public Boolean getEsActivo() {
        return esActivo;
    }

    public void setEsActivo(Boolean esActivo) {
        this.esActivo = esActivo;
    }
}
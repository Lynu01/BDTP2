package com.example.demo.dto;

import java.math.BigDecimal;

public class EmpleadoDTO {

    private Long id;
    private String nombre;
    private String valorDocumentoIdentidad;
    private String nombrePuesto;
    private Double saldoVacaciones;

    public EmpleadoDTO(Long id, String nombre, String valorDocumentoIdentidad, String nombrePuesto, BigDecimal saldoVacacionesBD) {
        this.id = id;
        this.nombre = nombre;
        this.valorDocumentoIdentidad = valorDocumentoIdentidad;
        this.nombrePuesto = nombrePuesto;

        // Convertir BigDecimal a Double
        // Se añade una comprobación para evitar un error si el valor de la base de datos fuera nulo.
        if (saldoVacacionesBD != null) {
            this.saldoVacaciones = saldoVacacionesBD.doubleValue();
        } else {
            this.saldoVacaciones = 0.0;
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getValorDocumentoIdentidad() { return valorDocumentoIdentidad; }
    public String getNombrePuesto() { return nombrePuesto; }
    public Double getSaldoVacaciones() { return saldoVacaciones; }
}
package com.example.demo.dto;

// Esta clase es un simple contenedor para mostrar los datos en la tabla.
// No está conectada a la base de datos directamente.


public class EmpleadoDTO {

    private Long id;
    private String nombre;
    private String valorDocumentoIdentidad;
    private String nombrePuesto;
    private Double saldoVacaciones;

    // Constructor para facilitar la creación de objetos desde el Servicio
    public EmpleadoDTO(Long id, String nombre, String valorDocumentoIdentidad, String nombrePuesto, Double saldoVacaciones) {
        this.id = id;
        this.nombre = nombre;
        this.valorDocumentoIdentidad = valorDocumentoIdentidad;
        this.nombrePuesto = nombrePuesto;
        this.saldoVacaciones = saldoVacaciones;
    }

    // Getters que Thymeleaf usará para mostrar los datos en el HTML
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getValorDocumentoIdentidad() { return valorDocumentoIdentidad; }
    public String getNombrePuesto() { return nombrePuesto; }
    public Double getSaldoVacaciones() { return saldoVacaciones; }
}
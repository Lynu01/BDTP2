package com.example.demo.dto;

public class TipoMovimientoDTO {
    private Integer id;
    private String nombre;

    public TipoMovimientoDTO(Object id, String nombre) {
        this.id = ((Number) id).intValue();
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}
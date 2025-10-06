package com.example.demo.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "TipoMovimiento")
@NamedStoredProcedureQuery(
    name = "TipoMovimiento.listar",
    procedureName = "sp_ListarTiposMovimiento",
    resultClasses = TipoMovimiento.class
)
public class TipoMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipoAccion;
    
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

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }
}
package com.example.demo.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Empleado")
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(
        name = "Empleado.listarTodos",
        procedureName = "sp_ListarEmpleados",
        resultClasses = Empleado.class
    ),
    @NamedStoredProcedureQuery(
    name = "Empleado.listar",
    procedureName = "sp_ListarEmpleados",
    parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "inFiltro", type = String.class)
    },
    resultClasses = Empleado.class
)
,

    @NamedStoredProcedureQuery(
        name = "Empleado.consultarPorDocumento",
        procedureName = "sp_ConsultarEmpleado",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "valorDocumentoIdentidad", type = String.class)
        },
        resultClasses = Empleado.class
    ),
    @NamedStoredProcedureQuery(
        name = "Empleado.insertar",
        procedureName = "sp_InsertarEmpleado",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "Nombre", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "ValorDocumentoIdentidad", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "FechaContratacion", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "SaldoVacaciones", type = Double.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "EsActivo", type = Boolean.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "IdPuesto", type = Long.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "PostInIP", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "PostBy", type = String.class)
        }
    ),
    @NamedStoredProcedureQuery(
        name = "Empleado.actualizar",
        procedureName = "sp_ActualizarEmpleado",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "ValorDocumentoIdentidad", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "Nombre", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "FechaContratacion", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "SaldoVacaciones", type = Double.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "EsActivo", type = Integer.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "IdPuesto", type = Long.class)
        }
    ),
    @NamedStoredProcedureQuery(
        name = "Empleado.eliminar",
        procedureName = "sp_EliminarEmpleado",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "valorDocumentoIdentidad", type = String.class)
        }
    )
})

public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "IdPuesto", referencedColumnName = "id")
    private Puesto puesto;

    @Column(nullable = false, unique = true)
    private String valorDocumentoIdentidad;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private LocalDate fechaContratacion;

    @Column(nullable = false)
    private Double saldoVacaciones;

    @Column(name = "EsActivo")
    private Integer esActivo;


    // Constructores
    public Empleado() {
    }

    public Empleado(Puesto puesto, String valorDocumentoIdentidad, String nombre,
                    LocalDate fechaContratacion, Double saldoVacaciones, Integer EsActivo) {
        this.puesto = puesto;
        this.valorDocumentoIdentidad = valorDocumentoIdentidad;
        this.nombre = nombre;
        this.fechaContratacion = fechaContratacion;
        this.saldoVacaciones = saldoVacaciones;
        this.esActivo = esActivo;
    }

    // Getters y Setters
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

    public Integer getEsActivo() {
        return esActivo;
    }

    public void setEsActivo(Integer esActivo) {
        this.esActivo = esActivo;
    }
}

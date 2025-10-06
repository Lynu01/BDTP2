package com.example.demo.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Movimiento")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idEmpleado", referencedColumnName = "Id")
    private Empleado empleado; 

    @ManyToOne
    @JoinColumn(name = "idTipoMovimiento", referencedColumnName = "Id")
    private TipoMovimiento tipoMovimiento;

    @Column(name = "Fecha")
    private LocalDateTime fecha;

    @Column(name = "Monto")
    private Double monto;

    @Column(name = "IdPostByUser")
    private String postByUser;

    @Column(name = "PostInIP")
    private String postInIP;

    @Column(name = "PostTime")
    private LocalDateTime postTime;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getPostByUser() {
        return postByUser;
    }

    public void setPostByUser(String postByUser) {
        this.postByUser = postByUser;
    }

    public String getPostInIP() {
        return postInIP;
    }

    public void setPostInIP(String postInIP) {
        this.postInIP = postInIP;
    }

    public LocalDateTime getPostTime() {
        return postTime;
    }

    public void setPostTime(LocalDateTime postTime) {
        this.postTime = postTime;
    }
}
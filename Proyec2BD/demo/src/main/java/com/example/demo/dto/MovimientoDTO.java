package com.example.demo.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MovimientoDTO {

    private LocalDate fecha;
    private String tipoMovimiento;
    private Double monto;
    private Double nuevoSaldo;
    private String postByUser;
    private String postInIP;
    private LocalDateTime postTime;

    // Constructor para transformar los datos crudos del SP
    public MovimientoDTO(Date fechaSQL, String tipoMovimiento, BigDecimal montoBD, BigDecimal nuevoSaldoBD,
                         String postByUser, String postInIP, Timestamp postTimeSQL) {
        this.fecha = fechaSQL.toLocalDate();
        this.tipoMovimiento = tipoMovimiento;
        this.monto = montoBD.doubleValue();
        this.nuevoSaldo = nuevoSaldoBD.doubleValue();
        this.postByUser = postByUser;
        this.postInIP = postInIP;
        this.postTime = postTimeSQL.toLocalDateTime();
    }

    // Getters para que Thymeleaf los use
    public LocalDate getFecha() { return fecha; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public Double getMonto() { return monto; }
    public Double getNuevoSaldo() { return nuevoSaldo; }
    public String getPostByUser() { return postByUser; }
    public String getPostInIP() { return postInIP; }
    public LocalDateTime getPostTime() { return postTime; }
}
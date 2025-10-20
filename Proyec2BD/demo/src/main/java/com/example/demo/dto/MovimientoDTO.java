package com.example.demo.dto;

//import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date; // Importante: usar java.util.Date

public class MovimientoDTO {

    private LocalDate fecha;
    private String tipoMovimiento;
    private Double monto;
    private Double nuevoSaldo;
    private String postByUser;
    private String postInIP;
    private LocalDateTime postTime;

    /**
     * ---- CONSTRUCTOR CORREGIDO ----
     * Este constructor es más flexible y acepta los tipos de datos genéricos
     * que vienen de la base de datos (java.util.Date y Number)
     * para evitar errores de casting.
     */
    public MovimientoDTO(Date fecha, String tipoMovimiento, Number monto, Number nuevoSaldo,
                         String postByUser, String postInIP, Timestamp postTimeSQL) {
        
        // Convertimos cualquier tipo de fecha a LocalDate de forma segura
        this.fecha = new Timestamp(fecha.getTime()).toLocalDateTime().toLocalDate();
        
        this.tipoMovimiento = tipoMovimiento;
        
        // El método .doubleValue() funciona tanto para BigDecimal como para Double
        this.monto = monto.doubleValue();
        this.nuevoSaldo = nuevoSaldo.doubleValue();
        
        this.postByUser = postByUser;
        this.postInIP = postInIP;
        this.postTime = postTimeSQL.toLocalDateTime();
    }

    // --- Getters (sin cambios) ---
    public LocalDate getFecha() { return fecha; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public Double getMonto() { return monto; }
    public Double getNuevoSaldo() { return nuevoSaldo; }
    public String getPostByUser() { return postByUser; }
    public String getPostInIP() { return postInIP; }
    public LocalDateTime getPostTime() { return postTime; }
}
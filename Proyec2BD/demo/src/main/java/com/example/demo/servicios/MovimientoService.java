package com.example.demo.servicios;

import com.example.demo.dto.MovimientoDTO;
import com.example.demo.dto.TipoMovimientoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovimientoService {

    @Autowired
    private EntityManager entityManager;

    /**
     * ---- MÉTODO CORREGIDO ----
     * Llama al Stored Procedure y usa el nuevo constructor del MovimientoDTO
     * para mapear los resultados de forma segura, evitando errores de casting.
     */
    public List<MovimientoDTO> obtenerMovimientosPorDocumento(String documentoIdentidad) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_ListarMovimientos")
                .registerStoredProcedureParameter("inValorDocumentoIdentidad", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                
                .setParameter("inValorDocumentoIdentidad", documentoIdentidad)
                .setParameter("inIP", "127.0.0.1")
                .setParameter("inPostByUser", "David");

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = query.getResultList();

            // Mapeo directo y seguro gracias al nuevo constructor del DTO
            return resultados.stream().map(r -> new MovimientoDTO(
                (Date) r[1],          // r[1] es Timestamp, que es un subtipo de Date
                (String) r[2],
                (Number) r[3],        // r[3] es Double, que es un subtipo de Number
                (Number) r[4],        // r[4] es BigDecimal, que es un subtipo de Number
                (String) r[5],
                (String) r[6],
                (Timestamp) r[7]
            )).collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error al obtener los movimientos desde la base de datos.");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // --- Método obtenerTiposMovimiento (sin cambios) ---
    public List<TipoMovimientoDTO> obtenerTiposMovimiento() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_ListarTiposMovimiento");
        
        @SuppressWarnings("unchecked")
        List<Object[]> resultados = query.getResultList();
        
        return resultados.stream()
                .map(r -> new TipoMovimientoDTO(r[0], (String) r[1]))
                .collect(Collectors.toList());
    }
}
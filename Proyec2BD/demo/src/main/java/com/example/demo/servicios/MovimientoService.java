package com.example.demo.servicios;

import com.example.demo.dto.MovimientoDTO;
import com.example.demo.dto.TipoMovimientoDTO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovimientoService {

    @Autowired
    private EntityManager entityManager;

    public List<MovimientoDTO> obtenerMovimientosPorDocumento(String documentoIdentidad) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_ListarMovimientos")
                .registerStoredProcedureParameter("inValorDocumentoIdentidad", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                
                .setParameter("inValorDocumentoIdentidad", documentoIdentidad)
                .setParameter("inIP", "127.0.0.1")
                .setParameter("inPostByUser", "David"); // Temporal

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = query.getResultList();

            return resultados.stream().map(r -> new MovimientoDTO(
                (Date) r[1],
                (String) r[2],
                (BigDecimal) r[3],
                (BigDecimal) r[4],
                (String) r[5],
                (String) r[6],
                (Timestamp) r[7]
            )).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<TipoMovimientoDTO> obtenerTiposMovimiento() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_ListarTiposMovimiento");
        
        @SuppressWarnings("unchecked")
        List<Object[]> resultados = query.getResultList();
        
        return resultados.stream()
                .map(r -> new TipoMovimientoDTO(r[0], (String) r[1]))
                .collect(Collectors.toList());
    }

}
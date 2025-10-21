package com.example.demo.servicios;

import com.example.demo.dto.TipoMovimientoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovimientoService {

    @Autowired
    private EntityManager entityManager;

    // metodo que obtiene movimientos por documento llamando al SP
    public java.util.List<com.example.demo.dto.MovimientoDTO> obtenerMovimientosPorDocumento(
        String documentoIdentidad, String user, String ip) {
    try {
        jakarta.persistence.StoredProcedureQuery query =
            entityManager.createStoredProcedureQuery("dbo.sp_ListarMovimientos")
                .registerStoredProcedureParameter("inValorDocumentoIdentidad", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                .setParameter("inValorDocumentoIdentidad", documentoIdentidad)
                .setParameter("inIP", ip)
                .setParameter("inPostByUser", user);

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> resultados = query.getResultList();

        return resultados.stream().map(r -> new com.example.demo.dto.MovimientoDTO(
        (java.util.Date)     r[1],            // Fecha
        (String)             r[2],            // TipoMovimiento
        (Number)             r[3],            // Monto
        (Number)             r[4],            // NuevoSaldo
        (String)             r[5],            // PostByUser
        (String)             r[6],            // PostInIP
        (java.sql.Timestamp) r[7]             // PostTime
    )).collect(java.util.stream.Collectors.toList());

    } catch (Exception e) {
        e.printStackTrace();
        return java.util.Collections.emptyList();
    }
}

public int insertarMovimiento(String valorDocId,
                              Integer idTipoMovimiento,
                              java.math.BigDecimal monto,
                              String user,
                              String ip,
                              java.time.LocalDate fechaNullable) {
    try {
        var q = entityManager.createStoredProcedureQuery("dbo.sp_InsertarMovimiento")
            .registerStoredProcedureParameter("inValorDocumentoIdentidad", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("inIdTipoMovimiento", Integer.class, ParameterMode.IN)
            .registerStoredProcedureParameter("inMonto", java.math.BigDecimal.class, ParameterMode.IN)
            .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
            .registerStoredProcedureParameter("inFecha", java.sql.Date.class, ParameterMode.IN);

        q.setParameter("inValorDocumentoIdentidad", valorDocId);
        q.setParameter("inIdTipoMovimiento", idTipoMovimiento);
        q.setParameter("inMonto", monto);
        q.setParameter("inPostByUser", user);
        q.setParameter("inIP", ip);
        q.setParameter("inFecha", (fechaNullable == null) ? null : java.sql.Date.valueOf(fechaNullable));

        q.execute();
        Integer rc = (Integer) q.getOutputParameterValue("outResultCode");
        return (rc == null) ? -1 : rc;
    } catch (Exception e) {
        e.printStackTrace();
        return -1;
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
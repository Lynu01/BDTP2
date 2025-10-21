package com.example.demo.servicios;

import com.example.demo.dto.EmpleadoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmpleadoService {
    
    @Autowired
    private EntityManager entityManager;

    public int trazarIntentoBorrado(String doc, String user, String ip) {
        StoredProcedureQuery q = entityManager
            .createStoredProcedureQuery("dbo.sp_TrazarIntentoBorrado")
            .registerStoredProcedureParameter("inValorDocumentoIdentidad", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
            .setParameter("inValorDocumentoIdentidad", doc)
            .setParameter("inPostByUser", user)
            .setParameter("inIP", ip);

        q.execute();
        Integer rc = (Integer) q.getOutputParameterValue("outResultCode");
        return rc == null ? -1 : rc;
    }
    public List<EmpleadoDTO> listarEmpleadosConFiltro(String filtro, String user, String ip) {
    String filtroSP = (filtro == null) ? "" : filtro;
    try {
        var query = entityManager.createStoredProcedureQuery("dbo.sp_ListarEmpleados")
                .registerStoredProcedureParameter("inFiltro", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                .setParameter("inFiltro", filtroSP)
                .setParameter("inIP", ip)
                .setParameter("inPostByUser", user);

        @SuppressWarnings("unchecked")
        List<Object[]> resultados = query.getResultList();

        return resultados.stream().map(r -> new EmpleadoDTO(
                ((Number) r[0]).longValue(),
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (java.math.BigDecimal) r[4]
        )).collect(java.util.stream.Collectors.toList());

    } catch (Exception e) {
        e.printStackTrace();
        return java.util.Collections.emptyList();
    }
}

    public EmpleadoDTO consultarEmpleadoPorDocumento(String documento, String user, String ip) {
    try {
        var query = entityManager.createStoredProcedureQuery("dbo.sp_ConsultarEmpleado")
                .registerStoredProcedureParameter("inValorDocumentoIdentidad", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                .setParameter("inValorDocumentoIdentidad", documento)
                .setParameter("inIP", ip)
                .setParameter("inPostByUser", user);

        @SuppressWarnings("unchecked")
        List<Object[]> resultados = query.getResultList();
        if (resultados.isEmpty()) return null;

        Object[] r = resultados.get(0);
        return new EmpleadoDTO(0L, (String) r[1], (String) r[0], (String) r[2], (java.math.BigDecimal) r[3]);

    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
}
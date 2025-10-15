package com.example.demo.servicios;

import com.example.demo.dto.EmpleadoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpleadoService {

    @Autowired
    private EntityManager entityManager;

    public List<EmpleadoDTO> listarEmpleadosConFiltro(String filtro) {
        String filtroSP = (filtro == null) ? "" : filtro;

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_ListarEmpleados")
                    .registerStoredProcedureParameter("inFiltro", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                    
                    .setParameter("inFiltro", filtroSP)
                    .setParameter("inIP", "127.0.0.1")
                    .setParameter("inPostByUser", "David");

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = query.getResultList();

            // ESTA ES LA LÍNEA CORREGIDA
            // Ahora pasamos el BigDecimal directamente, sin convertirlo.
            return resultados.stream().map(r -> new EmpleadoDTO(
                    ((Number) r[0]).longValue(),      // Id
                    (String) r[1],                      // Nombre
                    (String) r[2],                      // ValorDocumentoIdentidad
                    (String) r[3],                      // NombrePuesto
                    (BigDecimal) r[4]                   // SaldoVacaciones (se pasa como BigDecimal)
            )).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public EmpleadoDTO consultarEmpleadoPorDocumento(String documento) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_ConsultarEmpleado")
                .registerStoredProcedureParameter("inValorDocumentoIdentidad", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                
                .setParameter("inValorDocumentoIdentidad", documento)
                .setParameter("inIP", "127.0.0.1")
                .setParameter("inPostByUser", "David");

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = query.getResultList();

            if (resultados.isEmpty()) {
                return null;
            }

            Object[] r = resultados.get(0);
            // El método de consulta ya estaba correcto, no necesita cambios.
            return new EmpleadoDTO(0L, (String) r[1], (String) r[0], (String) r[2], (BigDecimal) r[3]);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
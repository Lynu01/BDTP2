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
    private EntityManager entityManager; // Herramienta avanzada para interactuar con la BD

    public List<EmpleadoDTO> listarEmpleadosConFiltro(String filtro) {
        // Asegurarnos de que el filtro no sea nulo para pasarlo al SP
        String filtroSP = (filtro == null) ? "" : filtro;

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_ListarEmpleados")
                    .registerStoredProcedureParameter("inFiltro", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                    
                    .setParameter("inFiltro", filtroSP)
                    .setParameter("inIP", "127.0.0.1") // IP de ejemplo (esto se mejora con sesiones de usuario)
                    .setParameter("inPostByUser", "David"); // Usuario de ejemplo

            // Ejecutamos la consulta y obtenemos los resultados
            @SuppressWarnings("unchecked")
            List<Object[]> resultados = query.getResultList();

            // Convertimos la lista de resultados crudos en nuestra lista de "Fichas de Empleado" (DTOs)
            return resultados.stream().map(r -> new EmpleadoDTO(
                    ((Number) r[0]).longValue(),      // Id
                    (String) r[1],                      // Nombre
                    (String) r[2],                      // ValorDocumentoIdentidad
                    (String) r[3],                      // NombrePuesto
                    ((BigDecimal) r[4]).doubleValue()   // SaldoVacaciones
            )).collect(Collectors.toList());

        } catch (Exception e) {
            // En caso de un error en la base de datos, imprimimos el error y devolvemos una lista vacía
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
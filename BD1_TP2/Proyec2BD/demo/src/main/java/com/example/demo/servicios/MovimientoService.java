package com.example.demo.servicios;

import com.example.demo.entidades.Movimiento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MovimientoService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<Movimiento> obtenerMovimientosEmpleado(String valorDocumentoIdentidad, String ip) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_ListarMovimientos", Movimiento.class);
        query.registerStoredProcedureParameter("valorDocumentoIdentidad", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN);
        query.setParameter("valorDocumentoIdentidad", valorDocumentoIdentidad);
        query.setParameter("inIP", ip);
        return query.getResultList();
    }

    @Transactional
    public void guardarMovimiento(String valorDocumentoIdentidad, Integer idTipoMovimiento, LocalDate fecha, Double monto, String postByUser, String postInIP) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_InsertarMovimiento");
        query.registerStoredProcedureParameter("valorDocumentoIdentidad", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("idTipoMovimiento", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("fecha", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("monto", Double.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("postByUser", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("postInIP", String.class, ParameterMode.IN);

        query.setParameter("valorDocumentoIdentidad", valorDocumentoIdentidad);
        query.setParameter("idTipoMovimiento", idTipoMovimiento);
        query.setParameter("fecha", fecha.toString());
        query.setParameter("monto", monto);
        query.setParameter("postByUser", postByUser);
        query.setParameter("postInIP", postInIP);

        query.execute();
    }
}

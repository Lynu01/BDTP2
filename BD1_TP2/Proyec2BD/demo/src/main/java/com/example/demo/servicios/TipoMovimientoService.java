package com.example.demo.servicios;

import com.example.demo.entidades.TipoMovimiento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoMovimientoService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<TipoMovimiento> listarTiposMovimiento(String ip) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_ListarTiposMovimiento", TipoMovimiento.class);
        query.registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN);
        query.setParameter("inIP", ip);
        return query.getResultList();
    }
}

package com.example.demo.servicios;

import com.example.demo.entidades.Empleado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpleadoService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<Empleado> listarEmpleados(String ip, String postByUser) {
    StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_ListarEmpleados", Empleado.class);
    query.registerStoredProcedureParameter("inFiltro", String.class, ParameterMode.IN);
    query.registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN);
    query.registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN);
    query.registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT);
    query.setParameter("inFiltro", "");
    query.setParameter("inIP", ip);
    query.setParameter("inPostByUser", postByUser);
    query.execute();
    return query.getResultList();
}


    @Transactional
    public Empleado buscarEmpleado(String valorDocumentoIdentidad) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_ConsultarEmpleado", Empleado.class);
        query.registerStoredProcedureParameter("valorDocumentoIdentidad", String.class, ParameterMode.IN);
        query.setParameter("valorDocumentoIdentidad", valorDocumentoIdentidad);

        List<Empleado> resultados = query.getResultList();
        if (!resultados.isEmpty()) {
            return resultados.get(0);
        }
        return null;
    }

    @Transactional
    public void insertarEmpleado(Empleado empleado, String ip, String postByUser) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_InsertarEmpleado");
        query.registerStoredProcedureParameter("nombre", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("valorDocumentoIdentidad", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("fechaContratacion", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("saldoVacaciones", Double.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("EsActivo", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("idPuesto", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("postInIP", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("postBy", String.class, ParameterMode.IN);

        query.setParameter("nombre", empleado.getNombre());
        query.setParameter("valorDocumentoIdentidad", empleado.getValorDocumentoIdentidad());
        query.setParameter("fechaContratacion", empleado.getFechaContratacion().toString());
        query.setParameter("saldoVacaciones", empleado.getSaldoVacaciones());
        query.setParameter("EsActivo", empleado.getEsActivo());
        query.setParameter("idPuesto", empleado.getPuesto().getId());
        query.setParameter("postInIP", ip);
        query.setParameter("postBy", postByUser);

        query.execute();
    }

    @Transactional
    public void actualizarEmpleado(Empleado empleado) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_ActualizarEmpleado");
        query.registerStoredProcedureParameter("valorDocumentoIdentidad", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("nombre", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("fechaContratacion", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("saldoVacaciones", Double.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("EsActivo", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("idPuesto", Long.class, ParameterMode.IN);

        query.setParameter("valorDocumentoIdentidad", empleado.getValorDocumentoIdentidad());
        query.setParameter("nombre", empleado.getNombre());
        query.setParameter("fechaContratacion", empleado.getFechaContratacion().toString());
        query.setParameter("saldoVacaciones", empleado.getSaldoVacaciones());
        query.setParameter("EsActivo", empleado.getEsActivo());
        query.setParameter("idPuesto", empleado.getPuesto().getId());

        query.execute();
    }

    @Transactional
    public void eliminarEmpleado(String valorDocumentoIdentidad) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_EliminarEmpleado");
        query.registerStoredProcedureParameter("valorDocumentoIdentidad", String.class, ParameterMode.IN);
        query.setParameter("valorDocumentoIdentidad", valorDocumentoIdentidad);
        query.execute();
    }
}

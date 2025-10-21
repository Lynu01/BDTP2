package com.example.demo.servicios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BitacoraService {

    @Autowired
    private EntityManager em;

    // Método para registrar un evento en la bitácora usando el SP
    public void logEvento(int idTipoEvento, String descripcion, String user, String ip) {
        try {
            StoredProcedureQuery q = em.createStoredProcedureQuery("dbo.sp_Bitacora_Add")
                .registerStoredProcedureParameter("inIdTipoEvento", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inDescripcion", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inPostByUser", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                .setParameter("inIdTipoEvento", idTipoEvento)
                .setParameter("inDescripcion", descripcion)
                .setParameter("inPostByUser", user)
                .setParameter("inIP", ip);

            q.execute();
        } catch (Exception ignored) {
            // La DB ya registra errores graves en DBError, acá no se hace nada más
        }
    }
}

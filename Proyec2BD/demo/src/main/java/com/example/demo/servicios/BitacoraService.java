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

    /**
     * Registra un evento en la bitácora usando el SP dbo.sp_Bitacora_Add
     * @param idTipoEvento Id del catálogo (en tu caso, 7 = intento/cancelación de update)
     * @param descripcion  Texto descriptivo
     * @param user         Username responsable
     * @param ip           IP origen
     */
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

            q.execute(); // ignoramos el out si no lo ocupás
        } catch (Exception ignored) {
            // No reventamos la UX por un fallo de bitácora. La DB ya registra errores graves en DBError.
        }
    }
}

package com.example.demo.servicios;

import com.example.demo.dto.AttemptStatsDTO;
import com.example.demo.dto.ThrottleResultDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private EntityManager entityManager;

    public ThrottleResultDTO checkLoginThrottle(String username, String ip) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_CheckThrottle")
                .registerStoredProcedureParameter("inUsername", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outIsBlocked", Boolean.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("outSecondsRemaining", Integer.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                .setParameter("inUsername", username)
                .setParameter("inIP", ip);

            query.execute();

            boolean isBlocked = (Boolean) query.getOutputParameterValue("outIsBlocked");
            int secondsRemaining = (Integer) query.getOutputParameterValue("outSecondsRemaining");
            return new ThrottleResultDTO(isBlocked, secondsRemaining);

        } catch (Exception e) {
            System.err.println("Error al ejecutar sp_CheckThrottle: " + e.getMessage());
            return new ThrottleResultDTO(false, 0);
        }
    }

    public AttemptStatsDTO getAttemptStats(String username, String ip) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_GetLoginAttemptStats")
                .registerStoredProcedureParameter("inUsername", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("outAttempts5Min", Integer.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("outMaxAttempts", Integer.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                .setParameter("inUsername", username)
                .setParameter("inIP", ip);

            query.execute();

            int attempts = (Integer) query.getOutputParameterValue("outAttempts5Min");
            int max      = (Integer) query.getOutputParameterValue("outMaxAttempts");
            int rc       = (Integer) query.getOutputParameterValue("outResultCode");
            return new AttemptStatsDTO(attempts, max, rc);
        } catch (Exception e) {
            System.err.println("Error al ejecutar sp_GetLoginAttemptStats: " + e.getMessage());
            // de 0 a 5 intentos, máximo 5
            return new AttemptStatsDTO(0, 5, 0);
        }
    }
}

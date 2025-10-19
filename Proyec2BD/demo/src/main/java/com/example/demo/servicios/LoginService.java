package com.example.demo.servicios;

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
                // --- Parámetros de Entrada (IN) ---
                .registerStoredProcedureParameter("inUsername", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN)
                
                // --- Parámetros de Salida (OUT) ---
                .registerStoredProcedureParameter("outIsBlocked", Boolean.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("outSecondsRemaining", Integer.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT)
                
                .setParameter("inUsername", username)
                .setParameter("inIP", ip);

            query.execute();

            // Recuperamos los valores de los parámetros de salida
            boolean isBlocked = (Boolean) query.getOutputParameterValue("outIsBlocked");
            int secondsRemaining = (Integer) query.getOutputParameterValue("outSecondsRemaining");

            return new ThrottleResultDTO(isBlocked, secondsRemaining);

        } catch (Exception e) {
            System.err.println("Error al ejecutar sp_CheckLoginThrottle: " + e.getMessage());
            // En caso de error, asumimos que no está bloqueado para no impedir el acceso.
            return new ThrottleResultDTO(false, 0);
        }
    }
}
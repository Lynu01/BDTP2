package com.example.demo.servicios;

import com.example.demo.entidades.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public boolean login(String nombre, String clave, String ip) {
    StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_Login");
    query.registerStoredProcedureParameter("nombre", String.class, ParameterMode.IN);
    query.registerStoredProcedureParameter("clave", String.class, ParameterMode.IN);
    query.registerStoredProcedureParameter("inIP", String.class, ParameterMode.IN);
    query.registerStoredProcedureParameter("outResultCode", Integer.class, ParameterMode.OUT);

    
    query.setParameter("nombre", nombre);
    query.setParameter("clave", clave);
    query.setParameter("inIP", ip);
    query.execute();

    Integer resultCode = (Integer) query.getOutputParameterValue("outResultCode");
    System.out.println("Código de resultado login: " + resultCode); 
    return resultCode != null && resultCode == 0;
}
}
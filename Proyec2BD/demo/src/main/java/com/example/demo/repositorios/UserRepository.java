package com.example.demo.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import java.util.Map; 

import com.example.demo.entidades.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByNombre(String nombre);

    // Procedimiento para iniciar sesión
    @Procedure(name = "dbo.sp_Login", outputParameterName = "outResultCode")
    Integer sp_Login(
        @Param("inUsername") String username,
        @Param("inPassword") String password,
        @Param("inIP") String ip
    );


    @Procedure(name = "dbo.sp_CheckThrottle")
    Map<String, Object> sp_CheckThrottle(
        @Param("inUsername") String username,
        @Param("inIP") String ip
    );

}
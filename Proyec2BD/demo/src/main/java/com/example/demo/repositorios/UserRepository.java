package com.example.demo.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import java.util.Map; 

import com.example.demo.entidades.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByNombre(String nombre);

    /**
     * Llama al procedimiento almacenado sp_Login para autenticar un usuario.
     * Este SP se encarga de validar las credenciales y registrar el intento en la bitácora.
     * Devuelve 0 si es exitoso, o un código de error si falla.
     */
    @Procedure(name = "dbo.sp_Login", outputParameterName = "outResultCode")
    Integer sp_Login(
        @Param("inUsername") String username,
        @Param("inPassword") String password,
        @Param("inIP") String ip
    );

    /**
     * Llama al procedimiento almacenado para verificar si un usuario está bloqueado
     * por exceso de intentos fallidos. Devuelve el número de intentos.
     *
    *@Procedure(name = "dbo.sp_BloqueoUsuario", outputParameterName = "outIntentosFallidos")
    *Integer sp_BloqueoUsuario(
        *@Param("inUsername") String username,
        *@Param("inIP") String ip
    *);
    **/

    /**
     * ---- NUEVO MÉTODO ----
     * Llama al procedimiento que verifica si un usuario está bloqueado
     * y devuelve el estado y el tiempo restante.
     * Los parámetros de salida se devuelven en un Map.
     */
    @Procedure(name = "dbo.sp_CheckThrottle")
    Map<String, Object> sp_CheckThrottle(
        @Param("inUsername") String username,
        @Param("inIP") String ip
    );

}
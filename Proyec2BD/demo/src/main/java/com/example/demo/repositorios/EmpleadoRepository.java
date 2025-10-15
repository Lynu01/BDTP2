package com.example.demo.repositorios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.example.demo.entidades.Empleado;
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Empleado findByValorDocumentoIdentidad(String valorDocumentoIdentidad);
     @Procedure(name = "dbo.sp_InsertarEmpleado", outputParameterName = "outResultCode")

    Integer sp_InsertarEmpleado(
        @Param("inValorDocumentoIdentidad") String valorDocumentoIdentidad,
        @Param("inIP") String ip,
        @Param("inNombre") String nombre,
        @Param("inIdPuesto") Long idPuesto,
        @Param("inPostByUser") String postByUser
    );

      @Procedure(name = "dbo.sp_EliminarEmpleado", outputParameterName = "outResultCode")
    Integer sp_EliminarEmpleado(
        @Param("inValorDocumentoIdentidad") String valorDocumentoIdentidad,
        @Param("inPostByUser") String postByUser,
        @Param("inIP") String ip
    );

    @Procedure(name = "dbo.sp_ActualizarEmpleado", outputParameterName = "outResultCode")
    Integer sp_ActualizarEmpleado(
        @Param("inValorDocumentoIdentidad") String valorDocumentoIdentidad, // La cédula original
        @Param("inNuevoValorDocumentoIdentidad") String nuevoValorDocumentoIdentidad,
        @Param("inNuevoNombre") String nuevoNombre,
        @Param("inNuevoIdPuesto") Long nuevoIdPuesto,
        @Param("inPostByUser") String postByUser,
        @Param("inIP") String ip
    );
}

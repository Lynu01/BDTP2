package com.example.demo.repositorios;

import com.example.demo.entidades.Empleado;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository {

    // Obtener todos los empleados
    @Procedure(name = "sp_obtener_empleados")
    List<Empleado> obtenerEmpleados();

    // Buscar un empleado por valorDocumentoIdentidad
    @Procedure(name = "sp_buscar_empleado_por_documento")
    Optional<Empleado> buscarEmpleadoPorDocumento(@Param("valorDocumentoIdentidad") String valorDocumentoIdentidad);

    // Insertar un nuevo empleado
    @Procedure(name = "sp_insertar_empleado")
    void insertarEmpleado(
        @Param("nombre") String nombre,
        @Param("valorDocumentoIdentidad") String valorDocumentoIdentidad,
        @Param("fechaContratacion") String fechaContratacion,
        @Param("saldoVacaciones") Double saldoVacaciones,
        @Param("esActivo") Boolean esActivo,
        @Param("idPuesto") Long idPuesto,
        @Param("postInIP") String postInIP,
        @Param("postBy") String postBy
    );

    // Actualizar empleado
    @Procedure(name = "sp_actualizar_empleado")
    void actualizarEmpleado(
        @Param("valorDocumentoIdentidad") String valorDocumentoIdentidad,
        @Param("nombre") String nombre,
        @Param("fechaContratacion") String fechaContratacion,
        @Param("saldoVacaciones") Double saldoVacaciones,
        @Param("esActivo") Boolean esActivo,
        @Param("idPuesto") Long idPuesto
    );

    // Eliminar empleado
    @Procedure(name = "sp_eliminar_empleado")
    void eliminarEmpleado(@Param("valorDocumentoIdentidad") String valorDocumentoIdentidad);
}

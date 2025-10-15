package com.example.demo.repositorios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.example.demo.entidades.Empleado;
import com.example.demo.entidades.Movimiento;
import java.util.List;
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    // Buscar movimientos de un empleado
    List<Movimiento> findByEmpleado(Empleado empleado);

    @Procedure(name = "dbo.sp_InsertarMovimiento", outputParameterName = "outResultCode")
    Integer sp_InsertarMovimiento(
        @Param("inValorDocumentoIdentidad") String valorDocumentoIdentidad,
        @Param("inIdTipoMovimiento") Integer idTipoMovimiento,
        @Param("inMonto") Double monto,
        @Param("inPostByUser") String postByUser,
        @Param("inIP") String ip
    );
}
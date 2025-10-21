package com.example.demo.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.example.demo.entidades.Empleado;
import com.example.demo.entidades.Movimiento;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByEmpleado(Empleado empleado);

    @Procedure(procedureName = "dbo.sp_InsertarMovimiento", outputParameterName = "outResultCode")
    Integer sp_InsertarMovimiento(
        @Param("inValorDocId") String valorDocId,
        @Param("inIdTipoMovimiento") Integer idTipoMovimiento,
        @Param("inFecha") Date fecha,
        @Param("inMonto") BigDecimal monto,
        @Param("inUserName") String userName,
        @Param("inIP") String ip
    );
}

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

    // Buscar movimientos de un empleado (no cambia)
    List<Movimiento> findByEmpleado(Empleado empleado);

    // Alineado al SP: inValorDocumentoIdentidad, inIdTipoMovimiento, inMonto(DECIMAL),
    // inPostByUser, inIP, outResultCode, inFecha (opcional, se puede mandar null)
    @Procedure(procedureName = "dbo.sp_InsertarMovimiento", outputParameterName = "outResultCode")
    Integer sp_InsertarMovimiento(
        @Param("inValorDocumentoIdentidad") String valorDocumentoIdentidad,
        @Param("inIdTipoMovimiento") Integer idTipoMovimiento,
        @Param("inMonto") BigDecimal monto,
        @Param("inPostByUser") String postByUser,
        @Param("inIP") String ip,
        @Param("inFecha") Date fecha // puede ser null; el SP usa GETDATE() si viene null
    );
}

package com.example.demo.repositorios;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entidades.Empleado;
import com.example.demo.entidades.Movimiento;
import java.util.List;
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    // Buscar movimientos de un empleado
    List<Movimiento> findByEmpleado(Empleado empleado);
}
package com.example.demo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Empleado;
import com.example.demo.Entity.Movimiento;

@Service
public class MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepo;

    @Autowired
    private EmpleadoRepository empleadoRepo;

    public List<Movimiento> listarPorEmpleado(Long idEmpleado) throws Exception {
        Empleado emp = empleadoRepo.findById(idEmpleado).orElseThrow(() -> new Exception("Empleado no encontrado"));
        return movimientoRepo.findByEmpleadoOrderByFechaDesc(emp);
    }

    public Movimiento insertarMovimiento(Movimiento m) throws Exception {
        Empleado emp = m.getEmpleado();
        double nuevoSaldo = m.getTipoMovimiento().getTipoAccion().equals("Credito")
                ? emp.getSaldoVacaciones() + m.getMonto()
                : emp.getSaldoVacaciones() - m.getMonto();
        if (nuevoSaldo < 0) {
            throw new Exception("El saldo sería negativo");
        }
        emp.setSaldoVacaciones(nuevoSaldo);
        m.setNuevoSaldo(nuevoSaldo);
        empleadoRepo.save(emp);
        return movimientoRepo.save(m);
    }
}

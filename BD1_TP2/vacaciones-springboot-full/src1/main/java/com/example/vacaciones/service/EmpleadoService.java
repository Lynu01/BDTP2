package com.example.vacaciones.service;

import com.example.vacaciones.entity.Empleado;
import com.example.vacaciones.repository.EmpleadoRepository;
import com.example.vacaciones.repository.PuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepo;

    @Autowired
    private PuestoRepository puestoRepo;

    public List<Empleado> listarEmpleados(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return empleadoRepo.findAll(Sort.by("nombre").ascending());
        }
        if (filtro.matches("[a-zA-Z ]+")) {
            return empleadoRepo.findByNombreContainingIgnoreCase(filtro);
        } else if (filtro.matches("\\d+")) {
            return empleadoRepo.findByValorDocumentoIdentidadContaining(filtro);
        }
        return Collections.emptyList();
    }

    public Empleado insertarEmpleado(Empleado emp) throws Exception {
        if (empleadoRepo.findByNombre(emp.getNombre()).isPresent())
            throw new Exception("Empleado con ese nombre ya existe");
        if (empleadoRepo.findByValorDocumentoIdentidad(emp.getValorDocumentoIdentidad()).isPresent())
            throw new Exception("Empleado con ese documento ya existe");
        emp.setSaldoVacaciones(0.0);
        emp.setEsActivo(true);
        return empleadoRepo.save(emp);
    }

    public Empleado actualizarEmpleado(Long id, Empleado actualizado) throws Exception {
        Empleado existente = empleadoRepo.findById(id).orElseThrow(() -> new Exception("Empleado no encontrado"));
        if (!existente.getNombre().equals(actualizado.getNombre()) &&
            empleadoRepo.findByNombre(actualizado.getNombre()).isPresent())
            throw new Exception("Nombre duplicado");
        if (!existente.getValorDocumentoIdentidad().equals(actualizado.getValorDocumentoIdentidad()) &&
            empleadoRepo.findByValorDocumentoIdentidad(actualizado.getValorDocumentoIdentidad()).isPresent())
            throw new Exception("Documento duplicado");
        existente.setNombre(actualizado.getNombre());
        existente.setValorDocumentoIdentidad(actualizado.getValorDocumentoIdentidad());
        existente.setPuesto(actualizado.getPuesto());
        return empleadoRepo.save(existente);
    }

    public void borrarEmpleado(Long id) throws Exception {
        Empleado emp = empleadoRepo.findById(id).orElseThrow(() -> new Exception("Empleado no encontrado"));
        emp.setEsActivo(false);
        empleadoRepo.save(emp);
    }

    public Optional<Empleado> obtenerPorId(Long id) {
        return empleadoRepo.findById(id);
    }
}
package com.example.vacaciones.repository;

import com.example.vacaciones.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByValorDocumentoIdentidad(String valorDocumentoIdentidad);
    Optional<Empleado> findByNombre(String nombre);
    List<Empleado> findByNombreContainingIgnoreCase(String nombre);
    List<Empleado> findByValorDocumentoIdentidadContaining(String valor);
}
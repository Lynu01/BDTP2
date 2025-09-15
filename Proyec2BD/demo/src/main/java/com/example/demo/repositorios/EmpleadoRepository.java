package com.example.demo.repositorios;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entidades.Empleado;
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Empleado findByValorDocumentoIdentidad(String valorDocumentoIdentidad);
}

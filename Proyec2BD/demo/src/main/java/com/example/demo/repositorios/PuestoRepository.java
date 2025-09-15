package com.example.demo.repositorios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.entidades.Puesto;
@Repository
public interface PuestoRepository extends JpaRepository<Puesto, Long> {
}
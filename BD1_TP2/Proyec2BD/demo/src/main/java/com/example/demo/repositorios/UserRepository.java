package com.example.demo.repositorios;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entidades.User;
public interface UserRepository extends JpaRepository<User, Long> {
    User findByNombre(String nombre);
}
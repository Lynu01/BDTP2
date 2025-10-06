package com.example.demo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.entidades.Empleado;
import com.example.demo.repositorios.EmpleadoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

import org.springframework.transaction.annotation.Transactional;



import java.util.List;

@Service
public class EmpleadoService {

    @Autowired
    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional
    public List<Empleado> obtenerListaAscendente(){
        return empleadoRepository.sp_ObtenerEmpleadosOrdenados();
    }

    @Transactional
    public List<Empleado> obtenerListaDescendente(){
        return empleadoRepository.sp_ObtenerEmpleadosOrdenadosDesc();
    }

    @PersistenceContext
    private EntityManager entityManager; // Permite ejecutar procedimientos almacenados

    @Transactional
    public int guardar(Empleado empleado) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_InsertarEmpleado");

        // Registrar parámetros de entrada
        query.registerStoredProcedureParameter("NombreCompleto", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("Salario", Double.class, ParameterMode.IN);

        // Registrar parámetro de salida
        query.registerStoredProcedureParameter("Resultado", Integer.class, ParameterMode.OUT);

        // Establecer valores
        query.setParameter("NombreCompleto", empleado.getNombre());
        query.setParameter("Salario", empleado.getSalario());

        // Ejecutar el procedimiento
        query.execute();

        // Obtener el resultado de salida
        Integer resultado = (Integer) query.getOutputParameterValue("Resultado");
        return resultado;
    }
    

}

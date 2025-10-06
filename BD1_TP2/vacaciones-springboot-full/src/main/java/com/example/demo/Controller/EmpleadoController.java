package com.example.demo.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Empleado;
import com.example.demo.Service.BitacoraService;
import com.example.demo.Service.EmpleadoService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private BitacoraService bitacoraService;

    @GetMapping
    public List<Empleado> listarEmpleados(@RequestParam(required = false) String filtro,
                                          HttpServletRequest request) {
        if (filtro != null) {
            if (filtro.matches("[a-zA-Z ]+")) {
                bitacoraService.registrarEvento("Consulta con filtro de nombre", filtro, null, request.getRemoteAddr());
            } else if (filtro.matches("\\d+")) {
                bitacoraService.registrarEvento("Consulta con filtro de cedula", filtro, null, request.getRemoteAddr());
            }
        }
        return empleadoService.listarEmpleados(filtro);
    }

    @PostMapping
    public ResponseEntity<?> insertarEmpleado(@RequestBody Empleado emp, HttpServletRequest request) {
        try {
            Empleado nuevo = empleadoService.insertarEmpleado(emp);
            bitacoraService.registrarEvento("Insercion exitosa",
                    String.format("%s, %s, %s", nuevo.getValorDocumentoIdentidad(), nuevo.getNombre(), nuevo.getPuesto().getNombre()),
                    null, request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (Exception e) {
            bitacoraService.registrarEvento("Insercion no exitosa", e.getMessage(), null, request.getRemoteAddr());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Empleado emp, HttpServletRequest request) {
        try {
            Empleado actualizado = empleadoService.actualizarEmpleado(id, emp);
            bitacoraService.registrarEvento("Update exitoso",
                    actualizado.getNombre(), null, request.getRemoteAddr());
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            bitacoraService.registrarEvento("Update no exitoso", e.getMessage(), null, request.getRemoteAddr());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrar(@PathVariable Long id, HttpServletRequest request) {
        try {
            Optional<Empleado> emp = empleadoService.obtenerPorId(id);
            if (emp.isEmpty()) throw new Exception("Empleado no encontrado");
            bitacoraService.registrarEvento("Intento de borrado",
                    emp.get().getNombre(), null, request.getRemoteAddr());

            empleadoService.borrarEmpleado(id);
            bitacoraService.registrarEvento("Borrado exitoso",
                    emp.get().getNombre(), null, request.getRemoteAddr());
            return ResponseEntity.ok("Empleado desactivado");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

package com.example.demo.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Movimiento;
import com.example.demo.Service.BitacoraService;
import com.example.demo.Service.MovimientoService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private BitacoraService bitacoraService;

    @GetMapping("/{idEmpleado}")
    public List<Movimiento> listar(@PathVariable Long idEmpleado) throws Exception {
        return movimientoService.listarPorEmpleado(idEmpleado);
    }

    @PostMapping
    public ResponseEntity<?> insertar(@RequestBody Movimiento movimiento, HttpServletRequest request) {
        try {
            Movimiento creado = movimientoService.insertarMovimiento(movimiento);
            bitacoraService.registrarEvento("Insertar movimiento exitoso",
                    String.format("%s, %s, %.2f", movimiento.getEmpleado().getNombre(),
                            movimiento.getTipoMovimiento().getNombre(), movimiento.getMonto()),
                    movimiento.getPostByUser(), request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
            bitacoraService.registrarEvento("Intento de insertar movimiento",
                    e.getMessage(), movimiento.getPostByUser(), request.getRemoteAddr());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

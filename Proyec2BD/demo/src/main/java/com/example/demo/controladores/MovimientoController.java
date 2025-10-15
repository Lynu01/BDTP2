package com.example.demo.controladores;

import com.example.demo.entidades.Empleado;
import com.example.demo.repositorios.EmpleadoRepository;
import com.example.demo.repositorios.MovimientoRepository;
import com.example.demo.servicios.MovimientoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MovimientoController {
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private MovimientoRepository movimientoRepository;
    @Autowired
    private MovimientoService movimientoService;

    // MÉTODO PARA MOSTRAR EL FORMULARIO
    @GetMapping("/empleados/{empleadoId}/movimientos/nuevo")
    public String mostrarFormularioMovimiento(@PathVariable("empleadoId") Long empleadoId, Model model) {
        
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + empleadoId));
        
        model.addAttribute("empleado", empleado);
        model.addAttribute("tiposMovimiento", movimientoService.obtenerTiposMovimiento());
        
        return "agregarMovimiento";
    }

    // MÉTODO PARA GUARDAR EL MOVIMIENTO
    @PostMapping("/movimientos/guardar")
    public String guardarMovimiento(
            @RequestParam("empleadoId") Long empleadoId,
            @RequestParam("idTipoMovimiento") Integer idTipoMovimiento,
            @RequestParam("monto") Double monto,
            HttpServletRequest request) {

        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + empleadoId));

        String ip = request.getRemoteAddr();
        String usuarioActual = "David"; // Temporal

        Integer resultCode = movimientoRepository.sp_InsertarMovimiento(
            empleado.getValorDocumentoIdentidad(),
            idTipoMovimiento,
            monto,
            usuarioActual,
            ip
        );

        if (resultCode != null && resultCode == 0) {
            // Éxito: Volvemos a la lista de movimientos de ese empleado
            return "redirect:/empleados/" + empleadoId + "/movimientos";
        } else {
            // Error: Volvemos al formulario con un código de error
            return "redirect:/empleados/" + empleadoId + "/movimientos/nuevo?error=" + resultCode;
        }
    }
}
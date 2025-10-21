package com.example.demo.controladores;

import com.example.demo.entidades.Empleado;
import com.example.demo.repositorios.EmpleadoRepository;
import com.example.demo.repositorios.MovimientoRepository;
import com.example.demo.servicios.MovimientoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.sql.Date;

@Controller
public class MovimientoController {

    @Autowired private EmpleadoRepository empleadoRepository;
    @Autowired private MovimientoRepository movimientoRepository;
    @Autowired private MovimientoService movimientoService;

    // FORM NUEVO MOVIMIENTO
    @GetMapping("/empleados/{empleadoId}/movimientos/nuevo")
    public String mostrarFormularioMovimiento(@PathVariable("empleadoId") Long empleadoId, Model model) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + empleadoId));
        model.addAttribute("empleado", empleado);
        model.addAttribute("tiposMovimiento", movimientoService.obtenerTiposMovimiento());
        return "agregarMovimiento";
    }

    // Cancelar inserción → solo banner
    @GetMapping("/empleados/{empleadoId}/movimientos/cancelar")
    public String cancelarInsercionMovimiento(@PathVariable("empleadoId") Long empleadoId,
                                              RedirectAttributes ra) {
        ra.addFlashAttribute("warn", "Operación cancelada por el usuario.");
        return "redirect:/empleados/" + empleadoId + "/movimientos";
    }

    // GUARDAR MOVIMIENTO
    @PostMapping("/movimientos/guardar")
    public String guardarMovimiento(@RequestParam("empleadoId") Long empleadoId,
                                    @RequestParam("idTipoMovimiento") Integer idTipoMovimiento,
                                    @RequestParam("monto") String montoStr,
                                    HttpServletRequest request,
                                    RedirectAttributes ra) {

        // Validar monto
        BigDecimal monto;
        try {
            monto = new BigDecimal(montoStr).setScale(2, java.math.RoundingMode.HALF_UP);
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                ra.addFlashAttribute("warn", "El monto debe ser mayor a 0.");
                return "redirect:/empleados/" + empleadoId + "/movimientos";
            }
        } catch (NumberFormatException ex) {
            ra.addFlashAttribute("warn", "Monto inválido. Use números y punto decimal (ej: 2.50).");
            return "redirect:/empleados/" + empleadoId + "/movimientos";
        }

        // Datos de sesión
        String usuarioActual = (String) request.getSession().getAttribute("usuarioActual");
        if (usuarioActual == null || usuarioActual.isBlank()) usuarioActual = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        // Cédula del empleado
        String valorDoc = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + empleadoId))
                .getValorDocumentoIdentidad();

        // Fecha: dejamos que el SP ponga hoy si va null. Si quieres forzar hoy:
        Date fecha = null; // oppure: Date.valueOf(LocalDate.now());

        Integer resultCode = movimientoRepository.sp_InsertarMovimiento(
                valorDoc,
                idTipoMovimiento,
                monto,
                usuarioActual,
                ip,
                fecha
        );

        if (Integer.valueOf(0).equals(resultCode)) {
            ra.addFlashAttribute("success", "Movimiento insertado exitosamente.");
            return "redirect:/empleados/" + empleadoId + "/movimientos";
        }

        if (Integer.valueOf(50011).equals(resultCode)) {
            ra.addFlashAttribute("warn", "No se registró el movimiento: el monto deja el saldo negativo.");
        } else if (Integer.valueOf(50012).equals(resultCode)) {
            ra.addFlashAttribute("warn", "Empleado no encontrado o inactivo.");
        } else if (Integer.valueOf(50001).equals(resultCode)) {
            ra.addFlashAttribute("warn", "Usuario no válido para registrar el movimiento.");
        } else if (Integer.valueOf(50008).equals(resultCode)) {
            ra.addFlashAttribute("error", "Error general de la base de datos.");
        } else {
            ra.addFlashAttribute("warn", "No se completó la agregación del movimiento. (Código " + resultCode + ")");
        }

        return "redirect:/empleados/" + empleadoId + "/movimientos";
    }
}

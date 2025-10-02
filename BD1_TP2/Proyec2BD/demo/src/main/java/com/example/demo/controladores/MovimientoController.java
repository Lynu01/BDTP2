package com.example.demo.controladores;

import com.example.demo.entidades.Empleado;
import com.example.demo.repositorios.PuestoRepository;
import com.example.demo.servicios.EmpleadoService;
import com.example.demo.servicios.MovimientoService;
import com.example.demo.servicios.TipoMovimientoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MovimientoController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private TipoMovimientoService tipoMovimientoService;

    @GetMapping("/movimientos/buscar")
    public String buscarEmpleadoPorDocumento(@RequestParam(name = "documentoIdentidad", required = false) String documentoIdentidad, Model model, HttpServletRequest request) {
        if (documentoIdentidad == null || documentoIdentidad.isEmpty()) {
            model.addAttribute("error", "El documento de identidad es requerido.");
            return "movimientos";
        }

        String ip = request.getRemoteAddr();
        Empleado empleado = empleadoService.buscarEmpleado(documentoIdentidad);
        if (empleado != null) {
            model.addAttribute("empleado", empleado);
            model.addAttribute("movimientos", movimientoService.obtenerMovimientosEmpleado(documentoIdentidad, ip));
            String user = (String) request.getSession().getAttribute("usuario");
            model.addAttribute("empleados", empleadoService.listarEmpleados(ip, user));
            List<Empleado> empleados = empleadoService.listarEmpleados(ip, user);
            model.addAttribute("empleados", empleados);

            model.addAttribute("tiposMovimiento", tipoMovimientoService.listarTiposMovimiento(ip));
        } else {
            model.addAttribute("empleado", null);
            model.addAttribute("error", "Empleado no encontrado.");
        }
        return "movimientos";
    }

    @PostMapping("/movimientos/guardar")
    public String guardarMovimiento(@RequestParam("valorDocId") String valorDocumentoIdentidad,
                                     @RequestParam("idTipoMovimiento") Integer idTipoMovimiento,
                                     @RequestParam("fecha") String fecha,
                                     @RequestParam("monto") Double monto,
                                     @RequestParam("idPostByUser") String postByUser,
                                     @RequestParam("postInIp") String postInIP) {

        LocalDate fechaMovimiento = LocalDate.parse(fecha);
        movimientoService.guardarMovimiento(valorDocumentoIdentidad, idTipoMovimiento, fechaMovimiento, monto, postByUser, postInIP);

        return "redirect:/movimientos/buscar?documentoIdentidad=" + valorDocumentoIdentidad;
    }
}
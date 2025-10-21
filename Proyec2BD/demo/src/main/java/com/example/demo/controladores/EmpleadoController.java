package com.example.demo.controladores;

import com.example.demo.dto.EmpleadoDTO;
import com.example.demo.entidades.Empleado;
import com.example.demo.repositorios.EmpleadoRepository;
import com.example.demo.repositorios.PuestoRepository;
import com.example.demo.servicios.EmpleadoService;
import com.example.demo.servicios.MovimientoService;
import com.example.demo.servicios.BitacoraService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EmpleadoController {

    @Autowired private EmpleadoRepository empleadoRepository;
    @Autowired private PuestoRepository puestoRepository;
    @Autowired private MovimientoService movimientoService;
    @Autowired private EmpleadoService empleadoService;
    @Autowired private BitacoraService bitacoraService;

    @GetMapping("/empleados/editar/{id}/cancelar")
    public String cancelarEdicionEmpleado(@PathVariable("id") Long id,
                                          HttpServletRequest request,
                                          RedirectAttributes ra) {
        var empleado = empleadoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + id));

        String usuarioActual = (String) request.getSession().getAttribute("usuarioActual");
        if (usuarioActual == null || usuarioActual.isBlank()) usuarioActual = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        // IdTipoEvento = 7 (update NO exitoso por cancelación)
        bitacoraService.logEvento(
            7,
            "Intento de actualizar empleado (CANCELADO). Doc=" + empleado.getValorDocumentoIdentidad()
                + ", Nombre=" + empleado.getNombre(),
            usuarioActual,
            ip
        );

        ra.addFlashAttribute("warn", "No se editó el empleado (operación cancelada).");
        return "redirect:/empleados";
    }

    @GetMapping("/empleados/nuevo/cancelar")
    public String cancelarCreacionEmpleado(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "doc", required = false) String doc,
            HttpServletRequest request,
            RedirectAttributes ra) {

        String usuarioActual = (String) request.getSession().getAttribute("usuarioActual");
        if (usuarioActual == null || usuarioActual.isBlank()) usuarioActual = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        String desc = "Intento de insertar empleado (CANCELADO). "
                + "DocIntentado=" + (doc == null ? "(no provisto)" : doc.trim())
                + ", NombreIntentado=" + (nombre == null ? "(no provisto)" : nombre.trim());

        // IdTipoEvento = 5 (insert NO exitoso por cancelación)
        bitacoraService.logEvento(5, desc, usuarioActual, ip);

        ra.addFlashAttribute("warn", "No se agregó el empleado (operación cancelada).");
        return "redirect:/empleados";
    }

    // ===== LISTAR =====
    @GetMapping("/empleados")
    public String getEmpleados(@RequestParam(required = false) String filtro, Model model, HttpServletRequest request) {
        String filtroNorm = (filtro == null) ? "" : filtro.trim();
        String user = (String) request.getSession().getAttribute("usuarioActual");
        if (user == null || user.isBlank()) user = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        List<EmpleadoDTO> empleados = empleadoService.listarEmpleadosConFiltro(filtroNorm, user, ip);
        model.addAttribute("empleados", empleados);
        model.addAttribute("filtroActual", filtroNorm);
        return "empleados";
    }

    // ===== CREAR =====
    @GetMapping("/empleados/nuevo")
    public String nuevoEmpleadoForm(Model model) {
        Empleado empleado = new Empleado();
        model.addAttribute("empleado", empleado);
        model.addAttribute("puestos", puestoRepository.findAll());
        return "agregarEmpleado";
    }

    @PostMapping("/empleados/guardar")
    public String guardarEmpleado(@RequestParam("nombre") String nombre,
                                  @RequestParam("valorDocumentoIdentidad") String valorDocumentoIdentidad,
                                  @RequestParam("idPuesto") Long idPuesto,
                                  HttpServletRequest request,
                                  RedirectAttributes ra) {

        String ip = request.getRemoteAddr();
        String usuarioActual = (String) request.getSession().getAttribute("usuarioActual");
        if (usuarioActual == null || usuarioActual.isBlank()) usuarioActual = "UsuarioScripts";

        // SIEMPRE llamamos al SP para que trace (50009/50010/etc.)
        Integer resultCode = empleadoRepository.sp_InsertarEmpleado(
                valorDocumentoIdentidad, ip, nombre, idPuesto, usuarioActual
        );

        if (Integer.valueOf(0).equals(resultCode)) {
            ra.addFlashAttribute("success", "Empleado registrado exitosamente.");
            return "redirect:/empleados";
        }

        String msg;
        if (Integer.valueOf(50004).equals(resultCode))      msg = "Ya existe un empleado con esa cédula. (50004)";
        else if (Integer.valueOf(50005).equals(resultCode)) msg = "Ya existe un empleado con ese nombre. (50005)";
        else if (Integer.valueOf(50009).equals(resultCode)) msg = "Nombre inválido. Solo letras, espacios o guion. (50009)";
        else if (Integer.valueOf(50010).equals(resultCode)) msg = "La cédula debe ser solo numérica. (50010)";
        else if (Integer.valueOf(50008).equals(resultCode)) msg = "Error de datos o base de datos. (50008)";
        else                                               msg = "No se pudo crear el empleado. (Código " + resultCode + ")";
        ra.addFlashAttribute("error", msg);
        return "redirect:/empleados/nuevo";
    }

    // ===== EDITAR =====
    @GetMapping("/empleados/editar/{id}")
    public String editarEmpleadoForm(@PathVariable("id") Long id, Model model) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        model.addAttribute("empleado", empleado);
        model.addAttribute("puestos", puestoRepository.findAll());
        return "editarEmpleado";
    }

    @PostMapping("/empleados/actualizar")
    public String actualizarEmpleado(@RequestParam("id") Long id,
                                     @RequestParam("valorDocumentoIdentidadOriginal") String valorDocumentoIdentidadOriginal,
                                     @RequestParam("nombre") String nuevoNombre,
                                     @RequestParam("valorDocumentoIdentidad") String nuevoValorDocumentoIdentidad,
                                     @RequestParam("idPuesto") Long nuevoIdPuesto,
                                     HttpServletRequest request,
                                     RedirectAttributes ra) {

        String usuarioActual = (String) request.getSession().getAttribute("usuarioActual");
        if (usuarioActual == null || usuarioActual.isBlank()) usuarioActual = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        // SIN validaciones Java: que la base valide y trace
        Integer resultCode = empleadoRepository.sp_ActualizarEmpleado(
                valorDocumentoIdentidadOriginal,
                nuevoValorDocumentoIdentidad,
                nuevoNombre,
                nuevoIdPuesto,
                usuarioActual,
                ip
        );

        if (Integer.valueOf(0).equals(resultCode)) {
            ra.addFlashAttribute("success", "Empleado editado exitosamente.");
            return "redirect:/empleados";
        }

        String msg;
        if (Integer.valueOf(50006).equals(resultCode))      msg = "La nueva cédula ya pertenece a otro empleado. (50006)";
        else if (Integer.valueOf(50007).equals(resultCode)) msg = "El nuevo nombre ya pertenece a otro empleado. (50007)";
        else if (Integer.valueOf(50009).equals(resultCode)) msg = "Nombre inválido. Solo letras, espacios o guion. (50009)";
        else if (Integer.valueOf(50010).equals(resultCode)) msg = "La cédula debe ser solo numérica. (50010)";
        else if (Integer.valueOf(50008).equals(resultCode)) msg = "Error de datos/relación o base de datos. (50008)";
        else                                               msg = "No se pudo actualizar el empleado. (Código " + resultCode + ")";
        ra.addFlashAttribute("error", msg);
        return "redirect:/empleados/editar/" + id;
    }

    // ===== CONSULTAR =====
    @GetMapping("/empleados/consultar/{id}")
    public String consultarEmpleado(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + id));

        String user = (String) request.getSession().getAttribute("usuarioActual");
        if (user == null || user.isBlank()) user = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        EmpleadoDTO empleadoDTO = empleadoService.consultarEmpleadoPorDocumento(
                empleado.getValorDocumentoIdentidad(), user, ip);

        model.addAttribute("empleado", empleadoDTO);
        return "consultarEmpleado";
    }

    // ===== MOVIMIENTOS =====
    @GetMapping("/empleados/{id}/movimientos")
    public String listarMovimientos(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + id));

        String user = (String) request.getSession().getAttribute("usuarioActual");
        if (user == null || user.isBlank()) user = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        var movimientos = movimientoService.obtenerMovimientosPorDocumento(
                empleado.getValorDocumentoIdentidad(), user, ip);

        model.addAttribute("empleado", empleado);
        model.addAttribute("movimientos", movimientos);
        return "listarMovimientos";
    }

    // ===== BITÁCORA intento de borrar (JS) =====
    @GetMapping("/empleados/{doc}/borrar-intento")
    @ResponseBody
    public String trazarIntentoBorrado(@PathVariable("doc") String doc, HttpServletRequest request) {
        String usuarioActual = (String) request.getSession().getAttribute("usuarioActual");
        if (usuarioActual == null || usuarioActual.isBlank()) usuarioActual = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        int rc = empleadoService.trazarIntentoBorrado(doc, usuarioActual, ip);
        return (rc == 0) ? "OK" : ("ERR:" + rc);
    }

    // ===== ELIMINAR =====
    @PostMapping("/empleados/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable("id") Long id,
                                   HttpServletRequest request,
                                   RedirectAttributes ra) {
        Empleado emp = empleadoRepository.findById(id).orElse(null);

        String usuarioActual = (String) request.getSession().getAttribute("usuarioActual");
        if (usuarioActual == null || usuarioActual.isBlank()) usuarioActual = "UsuarioScripts";
        String ip = request.getRemoteAddr();

        if (emp == null) {
            ra.addFlashAttribute("warn", "⚠️ No se eliminó el empleado (no encontrado).");
            return "redirect:/empleados";
        }

        Integer rc = empleadoRepository.sp_EliminarEmpleado(
                emp.getValorDocumentoIdentidad(), usuarioActual, ip
        );

        if (rc == null) {
            ra.addFlashAttribute("warn", "⚠️ No se eliminó el empleado (respuesta nula del SP).");
            return "redirect:/empleados";
        }

        switch (rc) {
            case 0      -> ra.addFlashAttribute("success", "✅ Se eliminó correctamente.");
            case 50012  -> ra.addFlashAttribute("warn", "⚠️ No se eliminó el empleado (no encontrado o inactivo).");
            case 50008  -> ra.addFlashAttribute("error", "Ocurrió un error en la base de datos. (50008)");
            default     -> ra.addFlashAttribute("warn", "⚠️ No se eliminó el empleado. (Código " + rc + ")");
        }
        return "redirect:/empleados";
    }
}

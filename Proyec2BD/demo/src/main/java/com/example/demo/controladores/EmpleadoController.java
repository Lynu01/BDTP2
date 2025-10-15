package com.example.demo.controladores;

import com.example.demo.dto.EmpleadoDTO;
import com.example.demo.entidades.Empleado;
import com.example.demo.entidades.Puesto;
import com.example.demo.repositorios.EmpleadoRepository;
import com.example.demo.repositorios.PuestoRepository;
import com.example.demo.servicios.EmpleadoService; // Importar el nuevo servicio
import com.example.demo.servicios.MovimientoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importar RequestParam

import java.util.List; // Importar List

import com.example.demo.dto.MovimientoDTO; // Importar el nuevo DTO
import com.example.demo.servicios.MovimientoService; // Importar el nuevo Servicio

@Controller
public class EmpleadoController {

    // Repositorios existentes (los usaremos para editar, guardar, etc.)
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private PuestoRepository puestoRepository;

    @Autowired
    private MovimientoService movimientoService;

    // AÑADIMOS NUESTRO NUEVO SERVICIO
    // Este servicio se encargará de llamar al Stored Procedure para listar/filtrar
    @Autowired
    private EmpleadoService empleadoService;

    // ESTE ES EL MÉTODO QUE CORREGIMOS
    // Ahora usa el servicio para obtener los DTOs en lugar de las entidades
    @GetMapping("/empleados")
    public String getEmpleados(@RequestParam(required = false) String filtro, Model model) {
        
        // 1. Llama al servicio que ejecuta el Stored Procedure
        List<EmpleadoDTO> empleados = empleadoService.listarEmpleadosConFiltro(filtro);
        
        // 2. Envía la lista de DTOs (el "paquete correcto") a la vista
        model.addAttribute("empleados", empleados);
        
        // 3. Guarda el filtro para mostrarlo de nuevo en la caja de texto
        model.addAttribute("filtroActual", filtro);
        
        // 4. Renderiza la página empleados.html
        return "empleados";
    }

    @GetMapping("/empleados/nuevo")
    public String nuevoEmpleadoForm(Model model) {
        Empleado empleado = new Empleado();
        model.addAttribute("empleado", empleado);
        model.addAttribute("puestos", puestoRepository.findAll()); 
        return "agregarEmpleado";
    }
    
    @PostMapping("/empleados/guardar")
    // --- REEMPLAZA TU MÉTODO guardarEmpleado CON ESTE ---
    
    public String guardarEmpleado(
            @RequestParam("nombre") String nombre,
            @RequestParam("valorDocumentoIdentidad") String valorDocumentoIdentidad,
            @RequestParam("idPuesto") Long idPuesto,
            HttpServletRequest request) {

        // Obtenemos los datos necesarios para la bitácora
        String ip = request.getRemoteAddr();
        String usuarioActual = "David"; // Temporal: Esto debería venir de la sesión del usuario logueado

        // Llamamos al Stored Procedure a través del repositorio
        Integer resultCode = empleadoRepository.sp_InsertarEmpleado(
            valorDocumentoIdentidad,
            ip,
            nombre,
            idPuesto,
            usuarioActual
        );

        // Evaluamos el resultado que nos devolvió la base de datos
        if (resultCode != null && resultCode == 0) {
            // Código 0 significa éxito. Redirigimos a la lista de empleados.
            return "redirect:/empleados";
        } else {
            // Si hubo un error (cédula duplicada, nombre duplicado, etc.),
            // redirigimos DE VUELTA al formulario de agregar, pasando el código de error.
            return "redirect:/empleados/nuevo?error=" + resultCode;
        }
    }
 @PostMapping("/empleados/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable("id") Long id, HttpServletRequest request) {
        
        // El SP necesita la cédula, no el ID. Primero buscamos al empleado.
        Empleado empleadoAEliminar = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado para eliminar: " + id));

        // Obtenemos los datos para la bitácora
        String ip = request.getRemoteAddr();
        String usuarioActual = "David"; // Temporal: Debería venir de la sesión

        // Llamamos al SP de borrado LÓGICO
        empleadoRepository.sp_EliminarEmpleado(
            empleadoAEliminar.getValorDocumentoIdentidad(),
            usuarioActual,
            ip
        );

        return "redirect:/empleados";
    }

    @GetMapping("/empleados/editar/{id}")
    public String editarEmpleadoForm(@PathVariable("id") Long id, Model model) {
        Empleado empleado = empleadoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        model.addAttribute("empleado", empleado);
        model.addAttribute("puestos", puestoRepository.findAll()); 
        return "editarEmpleado";
    }

     @PostMapping("/empleados/actualizar")
    public String actualizarEmpleado(
            @RequestParam("id") Long id,
            @RequestParam("valorDocumentoIdentidadOriginal") String valorDocumentoIdentidadOriginal,
            @RequestParam("nombre") String nuevoNombre,
            @RequestParam("valorDocumentoIdentidad") String nuevoValorDocumentoIdentidad,
            @RequestParam("idPuesto") Long nuevoIdPuesto,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        String usuarioActual = "David"; // Temporal

        Integer resultCode = empleadoRepository.sp_ActualizarEmpleado(
            valorDocumentoIdentidadOriginal,
            nuevoValorDocumentoIdentidad,
            nuevoNombre,
            nuevoIdPuesto,
            usuarioActual,
            ip
        );

        if (resultCode != null && resultCode == 0) {
            // Éxito
            return "redirect:/empleados";
        } else {
            // Error de validación, volvemos al formulario de edición
            return "redirect:/empleados/editar/" + id + "?error=" + resultCode;
        }
    }

     // --- AÑADE ESTE NUEVO MÉTODO PARA LA CONSULTA ---
    @GetMapping("/empleados/consultar/{id}")
    public String consultarEmpleado(@PathVariable("id") Long id, Model model) {
        
        // El SP de consulta usa la cédula. Primero la obtenemos.
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + id));

        // Usamos el servicio para llamar al SP
        EmpleadoDTO empleadoDTO = empleadoService.consultarEmpleadoPorDocumento(empleado.getValorDocumentoIdentidad());
        
        model.addAttribute("empleado", empleadoDTO);
        return "consultarEmpleado";
    }

    @GetMapping("/empleados/{id}/movimientos")
    public String listarMovimientos(@PathVariable("id") Long id, Model model) {
        // 1. Buscamos al empleado para tener sus datos básicos (nombre, saldo)
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + id));
        
        // 2. Usamos el nuevo servicio para obtener la lista de movimientos desde el SP
        List<MovimientoDTO> movimientos = movimientoService.obtenerMovimientosPorDocumento(empleado.getValorDocumentoIdentidad());
        
        // 3. Pasamos el empleado y la lista de movimientos al modelo
        model.addAttribute("empleado", empleado);
        model.addAttribute("movimientos", movimientos);
        
        // 4. ESTA ES LA LÍNEA QUE FALTABA
        // Le decimos a Spring que renderice la página "listarMovimientos.html"
        return "listarMovimientos";
    }


}


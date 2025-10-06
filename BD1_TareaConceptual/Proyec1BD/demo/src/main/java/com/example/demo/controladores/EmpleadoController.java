package com.example.demo.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Services.EmpleadoService;
import com.example.demo.entidades.Empleado;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping("/empleados")
public String listarEmpleados(@RequestParam(value = "orden", required = false) String orden,
                              @RequestParam(value = "mensaje", required = false) String mensaje, Model model) {
    List<Empleado> empleados;
    if ("desc".equals(orden)) {
        empleados = empleadoService.obtenerListaDescendente();
    } else {
        empleados = empleadoService.obtenerListaAscendente();
    }
    model.addAttribute("empleados", empleados);

    // Agregar mensaje de éxito si viene en la URL
    if (mensaje != null) {
        model.addAttribute("mensaje", mensaje);
    }
    return "empleados";
}

    @GetMapping("/empleados/nuevo")
    public String mostrarFormularioNuevoEmpleado(Model model){
        model.addAttribute("empleado", new Empleado());
        return "nuevoEmpleado";
    }

    @PostMapping("/empleados/guardar")
    public String guardarEmpleado(@ModelAttribute Empleado empleado, Model model) {

        //Validar que el nombre solo contenga letras o guiones
        if (!empleado.getNombre().matches("[A-Za-z\\s-]+")) {
            model.addAttribute("error", "El nombre solo puede contener letras, o separarse Nombre-Apellido.");
            return "nuevoEmpleado";
        }

        //Validar que el salario sea un número válido
        if ((empleado.getSalario() == null) || (empleado.getSalario().compareTo(BigDecimal.ZERO) <= 0)) {
            model.addAttribute("error", "El salario debe ser un número positivo válido.");
            return "nuevoEmpleado";
        }

        //Insertar en la base de datos
        int resultado = empleadoService.guardar(empleado);

        // Si el nombre ya existe, mostrar error
        if (resultado == -1) {
            model.addAttribute("error", "Error: El nombre de empleado ya existe.");
            return "nuevoEmpleado";
        }

        //Redirigir con mensaje de éxito
        return "redirect:/empleados?mensaje=Insercion+exitosa";
    }
}

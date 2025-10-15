package com.example.demo.controladores;

import com.example.demo.dto.EmpleadoDTO;
import com.example.demo.entidades.Empleado;
import com.example.demo.entidades.Puesto;
import com.example.demo.repositorios.EmpleadoRepository;
import com.example.demo.repositorios.PuestoRepository;
import com.example.demo.servicios.EmpleadoService; // Importar el nuevo servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importar RequestParam

import java.util.List; // Importar List

@Controller
public class EmpleadoController {

    // Repositorios existentes (los usaremos para editar, guardar, etc.)
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private PuestoRepository puestoRepository;

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
    public String guardarEmpleado(Empleado empleado) {
        // Aquí todavía usamos el repositorio, lo cual deberemos cambiar más adelante
        // para usar el Stored Procedure de inserción. Por ahora, esto funciona.
        empleadoRepository.save(empleado);
        return "redirect:/empleados";
    }

    @PostMapping("/empleados/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable("id") Long id) {
        // ¡CUIDADO! Esto sigue siendo un borrado físico.
        // Lo corregiremos en el requisito R4.
        empleadoRepository.deleteById(id);
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
    public String actualizarEmpleado(Empleado empleado) {
        // Esto también necesita ser actualizado para usar el Stored Procedure
        empleadoRepository.save(empleado);
        return "redirect:/empleados";
    }
}
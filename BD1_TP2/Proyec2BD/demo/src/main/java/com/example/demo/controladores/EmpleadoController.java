package com.example.demo.controladores;

import com.example.demo.entidades.Empleado;
import com.example.demo.repositorios.PuestoRepository;
import com.example.demo.servicios.EmpleadoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private PuestoRepository puestoRepository;

    @GetMapping("/empleados")
    public String getEmpleados(Model model, HttpServletRequest request, HttpSession session) {
        System.out.println("Entrando a /empleados");
        String ip = request.getRemoteAddr(); // captura la IP
        Object userAttr = session.getAttribute("user");
        if (userAttr == null) {
            return "redirect:/?error=session"; // Redirige al login
        }
        String user = userAttr.toString();


        model.addAttribute("empleados", empleadoService.listarEmpleados(ip, user));


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
    public String guardarEmpleado(Empleado empleado, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        empleadoService.insertarEmpleado(empleado, ip, "system"); // puedes poner aquí el usuario logueado después
        return "redirect:/empleados";
    }

    @PostMapping("/empleados/eliminar/{valorDocumentoIdentidad}")
    public String eliminarEmpleado(@PathVariable("valorDocumentoIdentidad") String valorDocumentoIdentidad) {
        empleadoService.eliminarEmpleado(valorDocumentoIdentidad);
        return "redirect:/empleados";
    }

    @GetMapping("/empleados/editar/{valorDocumentoIdentidad}")
    public String editarEmpleadoForm(@PathVariable("valorDocumentoIdentidad") String valorDocumentoIdentidad, Model model) {
        Empleado empleado = empleadoService.buscarEmpleado(valorDocumentoIdentidad);
        model.addAttribute("empleado", empleado);
        model.addAttribute("puestos", puestoRepository.findAll());
        return "editarEmpleado";
    }

    @PostMapping("/empleados/actualizar")
    public String actualizarEmpleado(Empleado empleado) {
        empleadoService.actualizarEmpleado(empleado);
        return "redirect:/empleados";
    }
}

package com.example.demo.controladores;
import com.example.demo.entidades.User;
import com.example.demo.repositorios.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
@Controller
public class LoginController {
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/")
    public String mostrarLogin() {
        return "login"; 
    }
    @PostMapping("/login")
    public String login(@RequestParam String nombre, @RequestParam String clave) {
        User user = userRepository.findByNombre(nombre);

        if (user != null && user.getClave().equals(clave)) {
            return "redirect:/empleados";
        } else {
            return "redirect:/?error=true"; 
        }
    }
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "Registrarse"; 
    }
    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre,
                                    @RequestParam String clave,
                                    @RequestParam String clave2) {

        if (!clave.equals(clave2)) {
            return "redirect:/registro?error=claves"; 
        }

        if (userRepository.findByNombre(nombre) != null) {
            return "redirect:/registro?error=usuario"; 
        }

        User nuevoUsuario = new User(nombre, clave);
        userRepository.save(nuevoUsuario);

        return "redirect:/"; 
    }

}

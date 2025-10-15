package com.example.demo.controladores;

import com.example.demo.entidades.User;
import com.example.demo.repositorios.UserRepository;
import jakarta.servlet.http.HttpServletRequest; // Importante añadir esto
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
    public String login(@RequestParam String nombre, @RequestParam String clave, HttpServletRequest request) {
        
        // Obtenemos la dirección IP del usuario. Es fundamental para la bitácora y el bloqueo.
        String ip = request.getRemoteAddr();

        // --- INICIA LÓGICA CORRECTA PARA R1 ---

        // 1. VERIFICAR BLOQUEO: Primero, le preguntamos a nuestro "vigilante" (el SP)
        // si el usuario tiene demasiados intentos fallidos.
        Integer intentosFallidos = userRepository.sp_BloqueoUsuario(nombre, ip);

        // El PDF dice que el bloqueo es con MÁS de 5 intentos. Lo ajustamos a >= 5 para que sea más seguro.
        if (intentosFallidos != null && intentosFallidos >= 5) {
            // Si está bloqueado, lo redirigimos con un mensaje de error específico.
            return "redirect:/?error=bloqueado";
        }

        // 2. INTENTAR LOGIN: Si no está bloqueado, le pedimos al "portero" (sp_Login) que verifique.
        Integer resultCode = userRepository.sp_Login(nombre, clave, ip);

        // 3. EVALUAR RESPUESTA: El SP nos devuelve un código. 0 es éxito.
        if (resultCode != null && resultCode == 0) {
            // Éxito. El usuario puede entrar al sistema.
            return "redirect:/empleados";
        } else {
            // Error. Credenciales inválidas. Lo mandamos de vuelta al login.
            return "redirect:/?error=invalido";
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
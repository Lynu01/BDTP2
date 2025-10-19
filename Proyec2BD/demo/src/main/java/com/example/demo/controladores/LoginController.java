package com.example.demo.controladores;

import com.example.demo.dto.ThrottleResultDTO;      // 1. Importar el nuevo DTO
import com.example.demo.entidades.User;
import com.example.demo.repositorios.UserRepository;
import com.example.demo.servicios.LoginService;      // 2. Importar el nuevo Servicio
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    // 3. Inyectar el nuevo LoginService
    @Autowired
    private LoginService loginService;

    @GetMapping("/")
    public String mostrarLogin() {
        return "login"; 
    }

    @PostMapping("/login")
    public String login(@RequestParam String nombre, @RequestParam String clave, HttpServletRequest request) {
        
        String ip = request.getRemoteAddr();

        // --- INICIA LA LÓGICA CORREGIDA ---

        // 4. VERIFICAR BLOQUEO: Llamamos al nuevo servicio, que es más robusto.
        ThrottleResultDTO throttleResult = loginService.checkLoginThrottle(nombre, ip);
        
        if (throttleResult.isBlocked()) {
            // Si está bloqueado, redirigimos con el error.
            return "redirect:/?error=bloqueado";
        }

        // 5. INTENTAR LOGIN: Si no está bloqueado, procedemos a llamar al SP de Login.
        Integer resultCode = userRepository.sp_Login(nombre, clave, ip);

        // 6. EVALUAR RESPUESTA:
        if (resultCode != null && resultCode == 0) {
            // Éxito, redirigimos a la lista de empleados.
            return "redirect:/empleados";
        } else {
            // Error (credenciales inválidas), redirigimos de vuelta al login.
            return "redirect:/?error=invalido";
        }
    }

    // --- MÉTODOS DE REGISTRO (SIN CAMBIOS) ---
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
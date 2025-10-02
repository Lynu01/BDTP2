package com.example.demo.controladores;

import com.example.demo.servicios.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.demo.entidades.User;



@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
public String login(@RequestParam String nombre,
                    @RequestParam String clave,
                    HttpServletRequest request) {

    String ip = request.getRemoteAddr();
    boolean loginExitoso = userService.login(nombre, clave, ip);
    System.out.println(loginExitoso);


    if (loginExitoso) {
        System.out.println("se mamo");
        HttpSession session = request.getSession();
        session.setAttribute("user", nombre);
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

        // Registro real de usuario no implementado aún
        return "redirect:/";
    }
}

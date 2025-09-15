package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Usuario;
import com.example.demo.Service.AuthService;
import com.example.demo.Service.BitacoraService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private BitacoraService bitacoraService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password,
                                   HttpServletRequest request) {
        try {
            Usuario user = authService.login(username, password);
            bitacoraService.registrarEvento("Login Exitoso", "", user, request.getRemoteAddr());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            bitacoraService.registrarEvento("Login No Exitoso", e.getMessage(), null, request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // En apps sin sesiones, esto puede ser simplemente registrar el evento
        bitacoraService.registrarEvento("Logout", "", null, request.getRemoteAddr());
        return ResponseEntity.ok("Logout exitoso");
    }
}

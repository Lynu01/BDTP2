package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Usuario;
import com.example.demo.Repository.UsuarioRepository;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    public Usuario login(String username, String password) throws Exception {
        Usuario user = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new Exception("Usuario inválido"));
        if (!user.getPassword().equals(password)) {
            throw new Exception("Contraseña inválida");
        }
        return user;
    }
}

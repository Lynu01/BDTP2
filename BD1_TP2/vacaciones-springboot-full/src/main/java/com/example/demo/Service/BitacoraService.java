package com.example.demo.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.BitacoraEvento;
import com.example.demo.Entity.TipoEvento;
import com.example.demo.Entity.Usuario;
import com.example.demo.Repository.BitacoraEventoRepository;
import com.example.demo.Repository.TipoEventoRepository;

@Service
public class BitacoraService {

    @Autowired
    private BitacoraEventoRepository bitacoraRepo;

    @Autowired
    private TipoEventoRepository tipoEventoRepo;

    public void registrarEvento(String tipoNombre, String descripcion, Usuario usuario, String ip) {
        TipoEvento tipo = tipoEventoRepo.findByNombre(tipoNombre).orElse(null);
        if (tipo == null) return;

        BitacoraEvento evento = new BitacoraEvento();
        evento.setTipoEvento(tipo);
        evento.setDescripcion(descripcion);
        evento.setPostByUser(usuario);
        evento.setPostInIP(ip);
        evento.setPostTime(LocalDateTime.now());
        bitacoraRepo.save(evento);
    }
}

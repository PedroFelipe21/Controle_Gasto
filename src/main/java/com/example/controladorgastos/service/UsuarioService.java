package com.example.controladorgastos.service;


import com.example.controladorgastos.DTOs.CadastroDTO;
import com.example.controladorgastos.DTOs.LoginDTO;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder encoder;


    public Usuario cadastrar(CadastroDTO dto) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario user = new Usuario();
        user.setNome(dto.getNome());
        user.setEmail(dto.getEmail());
        user.setSenha(encoder.encode(dto.getSenha()));

        return usuarioRepository.save(user);
    }

    // LOGIN
    public Usuario login(LoginDTO dto) {

        Usuario user = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!encoder.matches(dto.getSenha(), user.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        return user;
    }
}

package com.example.controladorgastos.controller;

import com.example.controladorgastos.DTOs.CadastroDTO;
import com.example.controladorgastos.DTOs.LoginDTO;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastro")
    public ResponseEntity<Usuario> cadastrar(@RequestBody CadastroDTO dto) {
        return ResponseEntity.ok(usuarioService.cadastrar(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginDTO dto, HttpSession session) {

      Usuario user =  usuarioService.login(dto);


        session.setAttribute("usuarioLogado", user);

        return ResponseEntity.ok(user);
    }
}

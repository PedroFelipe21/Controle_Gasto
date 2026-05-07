package com.example.controladorgastos.controller;

import com.example.controladorgastos.DTOs.CadastroDTO;
import com.example.controladorgastos.DTOs.LoginDTO;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/me")
    public ResponseEntity<Usuario> getUsuarioLogado(HttpSession session) {

        Usuario user = (Usuario) session.getAttribute("usuarioLogado");

        if (user == null) {
            return ResponseEntity.status(401).build(); // não logado
        }

        return ResponseEntity.ok(user);
    }
}

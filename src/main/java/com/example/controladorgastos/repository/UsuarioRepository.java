package com.example.controladorgastos.repository;

import com.example.controladorgastos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

 List<Usuario>findByEmail(String email);
}

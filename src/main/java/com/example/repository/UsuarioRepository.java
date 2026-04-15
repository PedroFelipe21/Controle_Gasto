package com.example.repository;

import com.example.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

 List<Usuario>findByEmail(String email);
}

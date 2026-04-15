package com.example.repository;

import com.example.entity.Meta;
import com.example.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface MetasRepository extends JpaRepository<Meta, Long> {


    List<Meta>findByUsuarios(Usuario usuario);


    List<Meta> findByUsuarioAndMes(Usuario usuario, String mes);
}

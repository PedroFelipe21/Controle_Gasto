package com.example.controladorgastos.repository;

import com.example.controladorgastos.entity.Meta;
import com.example.controladorgastos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetasRepository extends JpaRepository<Meta, Long> {


    List<Meta>findByUsuarios(Usuario usuario);


    List<Meta> findByUsuarioAndMes(Usuario usuario, String mes);
}

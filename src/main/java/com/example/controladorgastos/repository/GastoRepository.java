package com.example.controladorgastos.repository;

import com.example.controladorgastos.entity.Gasto;
import com.example.controladorgastos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByUsuario(Usuario usuario);


    //FILTROS ESPECIFICOS


    List<Gasto> findByUsuarioAndCategoria(Usuario usuario, String categoria);

    List<Gasto> findByUsuarioAndDataGastoBetween(Usuario usuario, LocalDate dataInicio, LocalDate dataFim);

    List<Gasto> findByUsuarioAndValorBetween(Usuario usuario, BigDecimal valorMin, BigDecimal valorMax);

    //Containig usado para ampliar a forma de busca
    List<Gasto>findByUsuarioAndDescricaoContaining(Usuario usuario, String descricao);

}

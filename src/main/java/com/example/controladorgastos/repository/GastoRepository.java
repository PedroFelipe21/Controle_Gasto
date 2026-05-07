package com.example.controladorgastos.repository;

import com.example.controladorgastos.entity.Gasto;
import com.example.controladorgastos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByUsuario(Usuario usuario);

    @Query("SELECT g FROM Gasto g WHERE g.usuario.id = :idUsuario " +
            "AND (:categoria IS NULL OR g.categoria = :categoria) " +
            "AND (:dataInicio IS NULL OR g.dataGasto >= :dataInicio) " +
            "AND (:dataFinal IS NULL OR g.dataGasto <= :dataFinal) " +
            "AND (:valMin IS NULL OR g.valor >= :valMin) " +
            "AND (:valMax IS NULL OR g.valor <= :valMax)")
    List<Gasto> buscarComTodosFiltros(Long idUsuario, String categoria, LocalDate dataInicio, LocalDate dataFinal, BigDecimal valMin, BigDecimal valMax);


    @Query("SELECT COALESCE(SUM(g.valor),0) FROM Gasto g WHERE g.usuario = :usuario")
    BigDecimal sumByUsuario(@Param("usuario") Usuario usuario);


}

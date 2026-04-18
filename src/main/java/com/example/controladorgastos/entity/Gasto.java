package com.example.controladorgastos.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


@Entity
@Table (name = "gastos")
public class Gasto {


    //Relacionamento
    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private BigDecimal valorGastos;

    private LocalDate dataGasto;

    private String categoria;



}

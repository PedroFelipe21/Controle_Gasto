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
@Table(name = "gasto")
@Access(AccessType.FIELD)
public class Gasto {

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idGasto")
    private Long id;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "valor")
    private BigDecimal valor;

    @Column(name = "dataGasto")
    private LocalDate dataGasto;

    @Column(name = "categoria")
    private String categoria;
}





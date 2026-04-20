package com.example.controladorgastos.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table (name="metas")
public class Meta {


    //Relacionamento
    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMeta")
    private Long id;

    private BigDecimal valor; //valor da meta que deseja atingir

    private BigDecimal rendaMensal;

    private String mes;
}

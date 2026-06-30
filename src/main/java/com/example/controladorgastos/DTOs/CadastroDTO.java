package com.example.controladorgastos.DTOs;


import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class CadastroDTO {
    private String nome;

    @Column(unique = true)
    private String email;

    private String senha;
}

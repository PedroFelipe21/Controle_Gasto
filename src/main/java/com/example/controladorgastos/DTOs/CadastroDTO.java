package com.example.controladorgastos.DTOs;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class CadastroDTO {
    private String nome;
    private String email;
    private String senha;
}

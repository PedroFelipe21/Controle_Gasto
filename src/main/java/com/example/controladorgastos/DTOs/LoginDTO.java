package com.example.controladorgastos.DTOs;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class LoginDTO {
    private String email;
    private String senha;
}

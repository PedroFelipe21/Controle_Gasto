package com.example.service;


import com.example.entity.Gasto;
import com.example.entity.Meta;
import com.example.entity.Usuario;
import com.example.repository.MetasRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetasRepository metasRepository;

    @Transactional
    public Meta salvarMeta(Meta meta){

        if (meta.getUsuario() == null) {
            throw new RuntimeException("Usuário obrigatório");
        }
        if (meta.getValor() == null || meta.getValor().compareTo(BigDecimal.ZERO) <=0){
            throw new RuntimeException("Valor invalido");
        }
        if (meta.getRendaMensal() == null || meta.getRendaMensal().compareTo(BigDecimal.ZERO) <=0)
        {
            throw new RuntimeException("Renda Mensal invalida");
        }

        return metasRepository.save(meta);
    }
    @Transactional
    public Meta atualizarMeta(Long id, Meta metaNova){

        Meta meta = metasRepository.findById(id).orElseThrow(()
                -> new RuntimeException("meta não encontrada"));

        if (metaNova.getValor().compareTo(metaNova.getRendaMensal()) > 0) {
            throw new RuntimeException("Meta não pode ser maior que a renda mensal");
        }

        meta.setValor(metaNova.getValor());
        meta.setRendaMensal(metaNova.getRendaMensal());
        meta.setMes(metaNova.getMes());

        return metasRepository.save(meta);
    }


    @Transactional
    public void deletarMeta(Long id){
        if(metasRepository.existsById(id)){
            throw new RuntimeException("meta não encontrada");
        }
        metasRepository.deleteById(id);
    }

    public List<Meta> listarGastoPorUsuario(Usuario usuario){
        return metasRepository.findByUsuarios(usuario);
    }

    public List<Meta> listarGastoPorMes(Usuario usuario, String mes){
        return metasRepository.findByUsuarioAndMes(usuario, mes);
    }
}

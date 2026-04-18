package com.example.controladorgastos.service;


import com.example.controladorgastos.entity.Meta;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.repository.MetasRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        if(!metasRepository.existsById(id)){
            throw new RuntimeException("meta não encontrada");
        }
        metasRepository.deleteById(id);
    }

    public List<Meta> listarMetaPorUsuario(Usuario usuario){

        return metasRepository.findByUsuarios(usuario);
    }


    public List<Meta> listarMetasPorMes(Usuario usuario, String mes){
        return metasRepository.findByUsuarioAndMes(usuario, mes);
    }


    public String situacaoFinanceira(Meta meta, BigDecimal totalGastos) {

        if (meta == null) {
            throw new RuntimeException("Meta não informada");
        }
        if (totalGastos == null) {
            totalGastos = BigDecimal.ZERO;
        }
        if (meta.getUsuario() == null) {
            throw new RuntimeException("Usuário não informado");
        }

        String nome = meta.getUsuario().getNome();
        BigDecimal rendaMensal = meta.getRendaMensal();

        if (rendaMensal == null) {
            throw new RuntimeException("Renda mensal não informada");
        }

        BigDecimal saldo = rendaMensal.subtract(totalGastos);
        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            return "Parabéns " + nome + ", neste mês você obteve lucro de R$ " + saldo;
        }

        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            return "Olá " + nome + ", neste mês você ficou no zero a zero, sem lucro e sem prejuízo.";
        }

        return "Atenção " + nome + ", neste mês você teve prejuízo de R$ " + saldo.abs();
    }


    public String analisePreditiva(BigDecimal totalGastos, int diaAtual, int totalDiasMes) {

        if (diaAtual <= 0 || totalDiasMes <= 0) {
            throw new RuntimeException("Datas inválidas");
        }
        BigDecimal mediaDiaria = totalGastos.divide(
                BigDecimal.valueOf(diaAtual),
                2,
                RoundingMode.HALF_UP
        );
        BigDecimal previsaoFinal = mediaDiaria.multiply(
                BigDecimal.valueOf(totalDiasMes)
        );

        return "Mantendo o ritmo atual, sua previsão de gastos no mês será de R$ " + previsaoFinal;
    }
}

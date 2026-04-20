package com.example.controladorgastos.service;


import com.example.controladorgastos.entity.Meta;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.repository.GastoRepository;
import com.example.controladorgastos.repository.MetasRepository;
import com.example.controladorgastos.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaService {
    private final UsuarioRepository usuarioRepository;
    private final GastoRepository gastoRepository;
    private final MetasRepository metasRepository;

    @Transactional
    public Meta salvarMeta(Meta meta){

        if (meta.getUsuario() == null || meta.getUsuario().getId() == null) {
            throw new RuntimeException("Usuário obrigatório");
        }

        if (meta.getValor() == null || meta.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor inválido");
        }

        if (meta.getRendaMensal() == null || meta.getRendaMensal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Renda Mensal inválida");
        }
        if (meta.getValor().compareTo(meta.getRendaMensal()) > 0) {
            throw new RuntimeException("Meta não pode ser maior que renda mensal");
        }


        Usuario usuario = usuarioRepository.findById(meta.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        meta.setUsuario(usuario);

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

        return metasRepository.findByUsuario(usuario);
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

        if (nome == null || nome.isBlank()) {
            nome = "Usuário";
        }

        BigDecimal rendaMensal = meta.getRendaMensal();

        if (rendaMensal == null) {
            throw new RuntimeException("Renda mensal não informada");
        }
        BigDecimal saldo = rendaMensal.subtract(totalGastos);

        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            return "Parabéns " + nome +
                    ", neste mês você conseguiu economizar R$ " + saldo;
        }
        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            return "Olá " + nome +
                    ", neste mês você ficou no zero a zero, sem lucro e sem prejuízo.";
        }
        return "Atenção " + nome +
                ", neste mês você teve prejuízo de R$ " + saldo.abs();
    }

    public String situacaoFinanceiraPorUsuario(Long idUsuario){

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        List<Meta> metas = metasRepository.findByUsuario(usuario);

        if (metas == null || metas.isEmpty()) {
            throw new RuntimeException("Meta não encontrada");
        }

        Meta meta = metas.get(0);
        BigDecimal totalGastos = gastoRepository.sumByUsuario(usuario);

        if (totalGastos == null) {
            totalGastos = BigDecimal.ZERO;
        }
        return situacaoFinanceira(meta, totalGastos);
    }


    //calculo da analise
    public String analisePreditiva(BigDecimal totalGastos, int diaAtual, int totalDiasMes) {

        if (totalGastos == null) {
            totalGastos = BigDecimal.ZERO;
        }
        if (diaAtual <= 0 || totalDiasMes <= 0 || diaAtual > totalDiasMes) {
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

    //Logica da analise
    public String analisePreditivaPorUsuario(Long idUsuario){

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        BigDecimal totalGastos = gastoRepository.sumByUsuario(usuario);

        if (totalGastos == null) {
            totalGastos = BigDecimal.ZERO;
        }

        int diaAtual = LocalDate.now().getDayOfMonth();
        int totalDiasMes = LocalDate.now().lengthOfMonth();

        return analisePreditiva(totalGastos, diaAtual, totalDiasMes);
    }



}

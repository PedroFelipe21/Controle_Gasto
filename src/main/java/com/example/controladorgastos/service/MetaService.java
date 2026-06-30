package com.example.controladorgastos.service;


import com.example.controladorgastos.entity.Gasto;
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
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MetaService {
    private final UsuarioRepository usuarioRepository;
    private final GastoRepository gastoRepository;
    private final MetasRepository metasRepository;

    @Transactional
    public Meta salvarMeta(Meta meta) {

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

        // junho/2026 para "2026-06"
        if (meta.getMes() != null) {
            meta.setMes(converterParaYearMonth(meta.getMes()));
        }

        // Verifica se já existe meta para este mês
        Meta metaExistente = metasRepository.findByUsuarioAndMes(usuario, meta.getMes())
                .stream()
                .findFirst()
                .orElse(null);

        if (metaExistente != null) {
            metaExistente.setValor(meta.getValor());
            metaExistente.setRendaMensal(meta.getRendaMensal());
            metaExistente.setMes(meta.getMes());
            return metasRepository.save(metaExistente);
        } else {
            return metasRepository.save(meta);
        }
    }

    // ← MÉTODO AUXILIAR
    private String converterParaYearMonth(String mesPorExtenso) {
        String mesLower = mesPorExtenso.toLowerCase().trim();

        // Extrai ano se existir (ex: "junho/2026" -> ano = 2026, mes = junho)
        String[] partes = mesLower.split("/");
        String mesNome = partes[0];
        String ano = partes.length > 1 ? partes[1] : String.valueOf(java.time.Year.now());

        int mesNumero = getMesNumero(mesNome);
        if (mesNumero == -1) {
            throw new RuntimeException("Mês inválido: " + mesNome);
        }

        // Retorna no formato "2026-06"
        return String.format("%s-%02d", ano, mesNumero + 1);
    }

    private int getMesNumero(String mesNome) {
        return switch (mesNome.trim()) {
            case "janeiro" -> 0;
            case "fevereiro" -> 1;
            case "março" -> 2;
            case "abril" -> 3;
            case "maio" -> 4;
            case "junho" -> 5;
            case "julho" -> 6;
            case "agosto" -> 7;
            case "setembro" -> 8;
            case "outubro" -> 9;
            case "novembro" -> 10;
            case "dezembro" -> 11;
            default -> -1;
        };
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


    //  Analise preditiva

    public Map<String, Object> analisePreditivaCompleta(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Gasto> gastos = gastoRepository.findByUsuario(usuario);
        if (gastos.isEmpty()) {
            throw new RuntimeException("Sem gastos cadastrados para análise");
        }

        // Agrupa e ordena gastos por mês
        Map<YearMonth, BigDecimal> gastosPorMes = new TreeMap<>();
        for (Gasto gasto : gastos) {
            YearMonth mes = YearMonth.from(gasto.getDataGasto());
            gastosPorMes.put(mes, gastosPorMes.getOrDefault(mes, BigDecimal.ZERO)
                    .add(gasto.getValor()));
        }

        List<YearMonth> meses = new ArrayList<>(gastosPorMes.keySet());
        List<BigDecimal> valores = new ArrayList<>();
        for (YearMonth mes : meses) {
            valores.add(gastosPorMes.get(mes));
        }

        // Busca TODAS as metas do usuário
        List<Meta> todasAsMetas = metasRepository.findByUsuario(usuario);

        // ← CALCULA SALDOS USANDO A META CORRETA DE CADA MÊS
        List<BigDecimal> saldos = new ArrayList<>();
        for (int i = 0; i < meses.size(); i++) {
            YearMonth mes = meses.get(i);
            BigDecimal valor = valores.get(i);

            // Procura a meta deste mês específico
            Meta metaDoMes = todasAsMetas.stream()
                    .filter(m -> m.getMes() != null &&
                            m.getMes().toLowerCase().contains(mes.toString().toLowerCase()))
                    .findFirst()
                    .orElse(null);


            BigDecimal saldo;
            if (metaDoMes != null) {
                saldo = metaDoMes.getValor().subtract(valor);
            } else {
                saldo = valor;
            }
            saldos.add(saldo);
        }

        // Calcula regressão linear dos SALDOS
        int n = saldos.size();
        BigDecimal somaX = BigDecimal.ZERO, somaY = BigDecimal.ZERO;
        BigDecimal somaXY = BigDecimal.ZERO, somaX2 = BigDecimal.ZERO;

        for (int i = 0; i < n; i++) {
            BigDecimal x = BigDecimal.valueOf(i + 1);
            BigDecimal y = saldos.get(i);
            somaX = somaX.add(x);
            somaY = somaY.add(y);
            somaXY = somaXY.add(x.multiply(y));
            somaX2 = somaX2.add(x.multiply(x));
        }

        BigDecimal numeradorB = BigDecimal.valueOf(n).multiply(somaXY).subtract(somaX.multiply(somaY));
        BigDecimal denominadorB = BigDecimal.valueOf(n).multiply(somaX2).subtract(somaX.multiply(somaX));
        BigDecimal b = numeradorB.divide(denominadorB, 4, RoundingMode.HALF_UP);
        BigDecimal a = somaY.subtract(b.multiply(somaX)).divide(BigDecimal.valueOf(n), 4, RoundingMode.HALF_UP);

        // Gera previsões
        List<Map<String, Object>> previsoes = new ArrayList<>();
        YearMonth ultimoMes = meses.get(meses.size() - 1);
        BigDecimal inclinacaoArredondada = b.setScale(2, RoundingMode.HALF_UP);

        for (int i = 1; i <= 3; i++) {
            YearMonth mesFuturo = ultimoMes.plusMonths(i);
            BigDecimal previsaoSaldo = a.add(b.multiply(BigDecimal.valueOf(meses.size() + i)));
            previsoes.add(Map.of("mes", mesFuturo.toString(), "valor", previsaoSaldo));
        }


        String tendencia;
        if (inclinacaoArredondada.compareTo(BigDecimal.ZERO) > 0) {
            tendencia = "MELHORANDO";
        } else {
            tendencia = "PIORANDO";
        }

        // Busca a ÚLTIMA meta para exibir (a mais recente)
        Meta ultimaMeta = todasAsMetas.stream()
                .reduce((first, second) -> second)
                .orElse(null);


        BigDecimal metaValor;
        if (ultimaMeta != null) {
            metaValor = ultimaMeta.getValor();
        } else {
            metaValor = null;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("meses", meses.stream().map(YearMonth::toString).toList());
        response.put("valoresHistoricos", valores);
        response.put("previsoes", previsoes);
        response.put("inclinacao", inclinacaoArredondada);
        response.put("meta", metaValor);
        response.put("tendencia", tendencia);

        return response;
    }


}

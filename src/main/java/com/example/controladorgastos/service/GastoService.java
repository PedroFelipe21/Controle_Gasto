package com.example.controladorgastos.service;


import com.example.controladorgastos.entity.Gasto;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.repository.GastoRepository;
import com.example.controladorgastos.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {
   private final UsuarioRepository usuarioRepository;
    private final GastoRepository gastoRepository;

    @Transactional //caso exista um erro, essa anotação faz o rollback
    public Gasto salvarGasto(Gasto gasto){

        if (gasto.getUsuario() == null) {
            throw new RuntimeException("Usuário obrigatório");
        }
        if (gasto.getValor() == null || gasto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor inválido");
        }
        if(gasto.getFormaPagamento().equals("credito") && gasto.getTotalParcelas() > 1){
          BigDecimal qtdParcelas = BigDecimal.valueOf(gasto.getTotalParcelas());
          BigDecimal valorParcelaCalculado = gasto.getValor().divide(qtdParcelas, 2, RoundingMode.HALF_UP);

          for (int i = 1; i <=gasto.getTotalParcelas(); i++){
              Gasto gastoParcela = new Gasto();

              gastoParcela.setDescricao(gasto.getDescricao());
              gastoParcela.setCategoria(gasto.getCategoria());
              gastoParcela.setValor(gasto.getValor());
              gastoParcela.setUsuario(gasto.getUsuario());
              gastoParcela.setFormaPagamento(gasto.getFormaPagamento());
              gastoParcela.setTotalParcelas(gasto.getTotalParcelas());

              // dados  parcela
              gastoParcela.setParcelaAtual(i);
              gastoParcela.setValorParcela(valorParcelaCalculado);
              gastoParcela.setDataGasto(gasto.getDataGasto().plusMonths(i - 1));

              gastoRepository.save(gastoParcela);
          }

            gasto.setValorParcela(valorParcelaCalculado);
          gasto.setParcelaAtual(1);
            gasto.setTotalParcelas(1);
            return gasto;
        }else {

            return gastoRepository.save(gasto);
        }
    }


    @Transactional
    public Gasto atualizarGastos(Long id, Gasto gastoNovo) {

        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado"));
        gasto.setDescricao(gastoNovo.getDescricao());
        gasto.setCategoria(gastoNovo.getCategoria());
        gasto.setDataGasto(gastoNovo.getDataGasto());
        gasto.setFormaPagamento(gastoNovo.getFormaPagamento());
        gasto.setTotalParcelas(gastoNovo.getTotalParcelas());
        gasto.setValor(gastoNovo.getValor());

        if (gastoNovo.getUsuario() != null && gastoNovo.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepository.findById(gastoNovo.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            gasto.setUsuario(usuario);
        }
        // Se era parcelado antes, deleta as parcelas antigas
        if (gasto.getFormaPagamento() != null &&
                gasto.getFormaPagamento().equalsIgnoreCase("credito") &&
                gasto.getParcelaAtual() != null &&
                gasto.getParcelaAtual() > 1) {

            gastoRepository.deleteById(id);
        }

        //  lógica de parcelas
        if (gastoNovo.getFormaPagamento().equalsIgnoreCase("credito") &&
                gastoNovo.getTotalParcelas() != null &&
                gastoNovo.getTotalParcelas() > 1) {

            BigDecimal qtdParcelas = BigDecimal.valueOf(gastoNovo.getTotalParcelas());
            BigDecimal valorParcelaCalculado = gastoNovo.getValor()
                    .divide(qtdParcelas, 2, RoundingMode.HALF_UP);

            for (int i = 1; i <= gastoNovo.getTotalParcelas(); i++) {
                Gasto gastoParcela = new Gasto();

                gastoParcela.setDescricao(gastoNovo.getDescricao());
                gastoParcela.setCategoria(gastoNovo.getCategoria());
                gastoParcela.setValor(gastoNovo.getValor());
                gastoParcela.setUsuario(gastoNovo.getUsuario());
                gastoParcela.setFormaPagamento(gastoNovo.getFormaPagamento());
                gastoParcela.setTotalParcelas(gastoNovo.getTotalParcelas());

                // Dados da parcela
                gastoParcela.setParcelaAtual(i);
                gastoParcela.setValorParcela(valorParcelaCalculado);
                gastoParcela.setDataGasto(gastoNovo.getDataGasto().plusMonths(i - 1));

                gastoRepository.save(gastoParcela);
            }

            // Atualiza o gasto principal como primeira parcela
            gasto.setValorParcela(valorParcelaCalculado);
            gasto.setParcelaAtual(1);
            gasto.setTotalParcelas(gastoNovo.getTotalParcelas());

        } else {

            // Se não for parcelado, limpa os campos
            gasto.setParcelaAtual(1);
            gasto.setValorParcela(null);
        }

        return gastoRepository.save(gasto);
    }
    @Transactional
    public void deletar(@PathVariable Long id){

        if(!gastoRepository.existsById(id)){
            throw new RuntimeException("Gasto não encontrado");
        }
         gastoRepository.deleteById(id);
    }

    public List<Gasto> listarGastoPorUsuario(Long idUsuario){

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return gastoRepository.findByUsuario(usuario);
    }
    //filtros de listagem
    private Usuario buscarUsuario(Long idUsuario){
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }


    public List<Gasto> buscarGastosFiltrados(Long idUsuario, String categoria, LocalDate dataInicio, LocalDate dataFinal, BigDecimal valMin, BigDecimal valMax) {

        // Tratamento de segurança: se a categoria vier como string vazia do JS, vira null
        if (categoria != null && categoria.isBlank()) {
            categoria = null;
        }

        return gastoRepository.buscarComTodosFiltros(
                idUsuario,
                categoria,
                dataInicio,
                dataFinal,
                valMin,
                valMax
        );
    }
}




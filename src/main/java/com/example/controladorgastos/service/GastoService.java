package com.example.controladorgastos.service;


import com.example.controladorgastos.entity.Gasto;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.repository.GastoRepository;
import com.example.controladorgastos.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {
   private final UsuarioRepository usuarioRepository;
    private final GastoRepository gastoRepository;

    @Transactional //faz o rollback
    public Gasto salvarGasto(Gasto gasto){

        if (gasto.getUsuario() == null) {
            throw new RuntimeException("Usuário obrigatório");
        }

        if (gasto.getValor() == null ||
                gasto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor inválido");
        }

        if(gasto.getFormaPagamento().equals("credito")
                && gasto.getTotalParcelas() > 1){


            String grupoParcelamento = UUID.randomUUID().toString();

            BigDecimal qtdParcelas =
                    BigDecimal.valueOf(gasto.getTotalParcelas());

            BigDecimal valorParcelaCalculado =
                    gasto.getValor().divide(
                            qtdParcelas,
                            2,
                            RoundingMode.HALF_UP
                    );

            for (int i = 1; i <= gasto.getTotalParcelas(); i++){

                Gasto gastoParcela = new Gasto();

                gastoParcela.setDescricao(gasto.getDescricao());
                gastoParcela.setCategoria(gasto.getCategoria());
                gastoParcela.setValor(gasto.getValor());
                gastoParcela.setUsuario(gasto.getUsuario());
                gastoParcela.setFormaPagamento(gasto.getFormaPagamento());
                gastoParcela.setTotalParcelas(gasto.getTotalParcelas());


                gastoParcela.setGrupoParcelamento(grupoParcelamento);

                gastoParcela.setParcelaAtual(i);
                gastoParcela.setValorParcela(valorParcelaCalculado);
                gastoParcela.setDataGasto(
                        gasto.getDataGasto().plusMonths(i - 1)
                );

                gastoRepository.save(gastoParcela);
            }

            gasto.setGrupoParcelamento(grupoParcelamento);

            gasto.setValorParcela(valorParcelaCalculado);
            gasto.setParcelaAtual(1);
            gasto.setTotalParcelas(1);

            return gasto;

        } else {

            gasto.setGrupoParcelamento(null);

            return gastoRepository.save(gasto);
        }
    }


    @Transactional
    public Gasto atualizarGastos(Long id, Gasto gastoNovo) {

        Gasto gastoAntigo = gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado"));

        // Busca usuário
        if (gastoNovo.getUsuario() != null &&
                gastoNovo.getUsuario().getId() != null) {

            Usuario usuario = usuarioRepository.findById(
                            gastoNovo.getUsuario().getId())
                    .orElseThrow(() ->
                            new RuntimeException("Usuário não encontrado"));

            gastoNovo.setUsuario(usuario);
        }

        // Remove registros antigos
        if (gastoAntigo.getGrupoParcelamento() != null &&
                !gastoAntigo.getGrupoParcelamento().isBlank()) {

            gastoRepository.deleteByGrupoParcelamento(
                    gastoAntigo.getGrupoParcelamento());

        } else {

            gastoRepository.deleteById(id);
        }

        // Crédito parcelado
        if ("credito".equalsIgnoreCase(gastoNovo.getFormaPagamento())
                && gastoNovo.getTotalParcelas() != null
                && gastoNovo.getTotalParcelas() > 1) {

            String grupoParcelamento = UUID.randomUUID().toString();

            BigDecimal qtdParcelas =
                    BigDecimal.valueOf(gastoNovo.getTotalParcelas());

            BigDecimal valorParcelaCalculado =
                    gastoNovo.getValor().divide(
                            qtdParcelas,
                            2,
                            RoundingMode.HALF_UP
                    );

            Gasto primeiraParcela = null;

            for (int i = 1; i <= gastoNovo.getTotalParcelas(); i++) {

                Gasto parcela = new Gasto();

                parcela.setDescricao(gastoNovo.getDescricao());
                parcela.setCategoria(gastoNovo.getCategoria());
                parcela.setValor(gastoNovo.getValor());
                parcela.setUsuario(gastoNovo.getUsuario());
                parcela.setFormaPagamento(gastoNovo.getFormaPagamento());
                parcela.setTotalParcelas(gastoNovo.getTotalParcelas());
                parcela.setGrupoParcelamento(grupoParcelamento);
                parcela.setParcelaAtual(i);
                parcela.setValorParcela(valorParcelaCalculado);
                parcela.setDataGasto(gastoNovo.getDataGasto().plusMonths(i - 1));

                Gasto salvar = gastoRepository.save(parcela);

                if (i == 1) {
                    primeiraParcela = salvar;
                }
            }

            return primeiraParcela;
        }

        // PIX, débito, dinheiro etc.
        gastoNovo.setGrupoParcelamento(null);
        gastoNovo.setParcelaAtual(1);
        gastoNovo.setTotalParcelas(1);
        gastoNovo.setValorParcela(null);

        return gastoRepository.save(gastoNovo);
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




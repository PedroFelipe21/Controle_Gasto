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

        gasto.setDataGasto(LocalDate.now());

        return gastoRepository.save(gasto);
    }


    @Transactional
    public Gasto atualizarGastos(Long id, Gasto gastoNovo){

        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado"));

        gasto.setDescricao(gastoNovo.getDescricao());
        gasto.setCategoria(gastoNovo.getCategoria());
        gasto.setDataGasto(gastoNovo.getDataGasto());
        gasto.setValor(gastoNovo.getValor());

        // GARANTIR que usuário não seja perdido na atualização
        if (gastoNovo.getUsuario() != null && gastoNovo.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepository.findById(gastoNovo.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            gasto.setUsuario(usuario);
            gasto.setDataGasto(LocalDate.now());
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




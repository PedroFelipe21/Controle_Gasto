package com.example.controladorgastos.service;


import com.example.controladorgastos.entity.Gasto;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.repository.GastoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {

    private final GastoRepository gastoRepository;

    @Transactional //caso exista um erro, essa anotação faz o rollback
    public Gasto salvarGasto(Gasto gasto){

       if(gasto.getValorGastos().compareTo(BigDecimal.ZERO)<=0){
           throw new RuntimeException("Valor invalido");
       }

      return  gastoRepository.save(gasto);

    }


    @Transactional
    public Gasto atualizarGastos(Long id, Gasto gastoNovo){

        Gasto gasto = gastoRepository.findById(id).orElseThrow(() -> new RuntimeException("Gasto não encontrado"));


        gasto.setDescricao(gastoNovo.getDescricao());
        gasto.setCategoria(gastoNovo.getCategoria());
        gasto.setDataGasto(gastoNovo.getDataGasto());
        gasto.setValorGastos(gastoNovo.getValorGastos());

        return gastoRepository.save(gasto);

    }


    @Transactional
    public void deletar(Long id){

        if(!gastoRepository.existsById(id)){
            throw new RuntimeException("Gasto não encontrado");
        }
         gastoRepository.deleteById(id);
    }

    public List<Gasto> listarGastoPorUsuario(Usuario usuario){
        return gastoRepository.findByUsuario(usuario);

    }

    //filtros de listagem

    public List<Gasto> listarPorCategoria(Usuario usuario,String categoria){
        return gastoRepository.findByUsuarioAndCategoria(usuario, categoria);
    }

    public List<Gasto> listarPorData(Usuario usuario, LocalDate dataInicio, LocalDate dataFinal){
        return gastoRepository.findByUsuarioAndDataGastoBetween(usuario, dataInicio, dataFinal);
    }

    public List<Gasto> listarPorValor(Usuario usuario, BigDecimal valorMin, BigDecimal valorMax){
        return gastoRepository.findByUsuarioAndValorBetween(usuario, valorMin, valorMax);
    }

    public List<Gasto> listarPorDescricao(Usuario usuario, String descricao){
        return gastoRepository.findByUsuarioAndDescricaoContaining(usuario, descricao);
    }


}




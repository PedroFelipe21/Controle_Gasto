package com.example.controladorgastos.controller;


import com.example.controladorgastos.entity.Gasto;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.service.GastoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService gastoService;


    @PostMapping
    public ResponseEntity<Gasto> salvar(@RequestBody Gasto gasto){
        return ResponseEntity.ok(gastoService.salvarGasto(gasto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarGasto(@PathVariable Long id){

        gastoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> bucarGastos(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFinal,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) BigDecimal valMin,
            @RequestParam(required = false) BigDecimal valMax,
            @RequestParam(required = false) Long idUsuario){

        if (categoria != null){
            return ResponseEntity.ok(gastoService.listarPorCategoria(idUsuario, categoria));
        }
        if (dataInicio != null && dataFinal != null){
            return ResponseEntity.ok(gastoService.listarPorData(idUsuario, dataInicio, dataFinal));
        }
        if (valMin != null && valMax != null){
            return ResponseEntity.ok(gastoService.listarPorValor(idUsuario, valMin, valMax));
        }

        return ResponseEntity.ok(gastoService.listarGastoPorUsuario(idUsuario));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Gasto>> buscarGastosPorUsuario(
            @PathVariable Long idUsuario){

        return ResponseEntity.ok(
                gastoService.listarGastoPorUsuario(idUsuario)
        );
    }
    @PutMapping("/{id}")
    public ResponseEntity<Gasto> alterarGasto(@PathVariable Long id,
                                              @RequestBody Gasto gastoNovo){

        return ResponseEntity.ok(gastoService.atualizarGastos(id, gastoNovo));
    }
}

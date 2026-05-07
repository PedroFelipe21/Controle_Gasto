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
    public ResponseEntity<List<Gasto>> buscarGastos(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFinal,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) BigDecimal valMin,
            @RequestParam(required = false) BigDecimal valMax,
            @RequestParam Long idUsuario) {

        // Agora ele não escolhe um IF, ele manda tudo de uma vez
        return ResponseEntity.ok(gastoService.buscarGastosFiltrados(idUsuario, categoria, dataInicio, dataFinal, valMin, valMax));
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

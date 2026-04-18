package com.example.controladorgastos.controller;


import com.example.controladorgastos.entity.Gasto;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.service.GastoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService gastoService;

    @PostMapping
    public Gasto salvarGasto(@RequestBody Gasto gasto){
        return gastoService.salvarGasto(gasto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarGasto(@PathVariable Long id){

        gastoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> bucarGastos(@RequestParam(required = false)LocalDate dataInicio,
                                         @RequestParam(required = false) LocalDate dataFinal,
                                         @RequestParam(required = false) String categoria,
                                         @RequestParam(required = false)BigDecimal valMin,
                                         @RequestParam(required = false) BigDecimal valMax,
                                         @RequestParam(required = false)Usuario usuario){


        if (categoria != null){
            return ResponseEntity.ok(gastoService.listarPorCategoria(usuario, categoria));
        }
        if (dataInicio != null && dataFinal != null){
            return ResponseEntity.ok(gastoService.listarPorData(usuario, dataInicio, dataFinal));
        }
        if (valMin != null && valMax != null){
            return ResponseEntity.ok(gastoService.listarPorValor(usuario, valMin, valMax));
        }
        return ResponseEntity.ok(gastoService.listarGastoPorUsuario(usuario));
        
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gasto> alterarGasto(@PathVariable Long id,
                                            @RequestBody Gasto gastoNovo){

        return ResponseEntity.accepted().body(gastoService.atualizarGastos(id, gastoNovo));
    }
}

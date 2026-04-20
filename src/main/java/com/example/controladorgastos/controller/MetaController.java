package com.example.controladorgastos.controller;

import com.example.controladorgastos.entity.Meta;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.repository.MetasRepository;
import com.example.controladorgastos.service.MetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/metas")
@RequiredArgsConstructor
public class MetaController {
    private final MetasRepository metasRepository;
    private final MetaService metasService;

    @PostMapping
     public Meta salvarMeta(@RequestBody Meta meta){

       return metasService.salvarMeta(meta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMeta(@PathVariable Long id){
        metasService.deletarMeta(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<Meta>> buscarMeta(@PathVariable Long id){

        Usuario usuario = new Usuario();
        usuario.setId(id);

        return ResponseEntity.ok(
                metasService.listarMetaPorUsuario(usuario)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meta> alterarMeta(
            @PathVariable Long id,
            @RequestBody Meta metaNova){

        return ResponseEntity.accepted()
                .body(metasService.atualizarMeta(id, metaNova));
    }

    @GetMapping("/situacao/{idUsuario}")
    public ResponseEntity<String> situacaoFinanceira(
            @PathVariable Long idUsuario){

        return ResponseEntity.ok(
                metasService.situacaoFinanceiraPorUsuario(idUsuario)
        );
    }

    @GetMapping("/previsao/{idUsuario}")
    public ResponseEntity<String> previsao(@PathVariable Long idUsuario){

        return ResponseEntity.ok(
                metasService.analisePreditivaPorUsuario(idUsuario)
        );
    }


}

package com.example.controladorgastos.controller;

import com.example.controladorgastos.entity.Meta;
import com.example.controladorgastos.entity.Usuario;
import com.example.controladorgastos.service.MetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metas")
@RequiredArgsConstructor
public class MetaController {

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

    @GetMapping
    public ResponseEntity<List<Meta>> buscarMeta(@RequestParam Usuario usuario){
        return ResponseEntity.ok().body(metasService.listarMetaPorUsuario(usuario));

    }

    @PutMapping("/{id}")
    public ResponseEntity<Meta> alterarMeta(@RequestParam Long id,
                                                   @RequestParam Meta metaNova){

        return ResponseEntity.accepted().body(metasService.atualizarMeta(id, metaNova));
    }
}

package com.guilhermepagio.aureus.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.service.ReceitaVariavelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/receitas-variaveis")
public class ReceitaVariavelController {

    private final ReceitaVariavelService receitaVariavelService;

    public ReceitaVariavelController(ReceitaVariavelService receitaVariavelService) {
        this.receitaVariavelService = receitaVariavelService;
    }

    @GetMapping
    public List<ReceitaVariavelResponseDTO> listar() {
        return receitaVariavelService.listar();
    }

    @PostMapping
    public ResponseEntity<ReceitaVariavelResponseDTO> criar(final @Valid @RequestBody ReceitaVariavelRequestDTO dto) {
        return ResponseEntity.ok(receitaVariavelService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceitaVariavelResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody ReceitaVariavelRequestDTO dto) {
        return ResponseEntity.ok(receitaVariavelService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
        boolean excluido = receitaVariavelService.excluir(id);
        if (!excluido) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

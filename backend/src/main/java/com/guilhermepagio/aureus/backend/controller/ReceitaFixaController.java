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

import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.service.ReceitaFixaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/receitas-fixas")
public class ReceitaFixaController {

    private final ReceitaFixaService receitaFixaService;

    public ReceitaFixaController(ReceitaFixaService receitaFixaService) {
        this.receitaFixaService = receitaFixaService;
    }

    @GetMapping
    public List<ReceitaFixaResponseDTO> listar() {
        return receitaFixaService.listar();
    }

    @PostMapping
    public ResponseEntity<ReceitaFixaResponseDTO> criar(final @Valid @RequestBody ReceitaFixaRequestDTO dto) {
        return ResponseEntity.ok(receitaFixaService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceitaFixaResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody ReceitaFixaRequestDTO dto) {
        return ResponseEntity.ok(receitaFixaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
        boolean excluido = receitaFixaService.excluir(id);
        if (!excluido) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

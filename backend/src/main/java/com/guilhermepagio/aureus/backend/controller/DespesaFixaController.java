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

import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.service.DespesaFixaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/despesas-fixas")
public class DespesaFixaController {

    private final DespesaFixaService despesaFixaService;

    public DespesaFixaController(DespesaFixaService despesaFixaService) {
        this.despesaFixaService = despesaFixaService;
    }

    @GetMapping
    public List<DespesaFixaResponseDTO> listar() {
        return despesaFixaService.listar();
    }

    @PostMapping
    public ResponseEntity<DespesaFixaResponseDTO> criar(final @Valid @RequestBody DespesaFixaRequestDTO dto) {
        return ResponseEntity.ok(despesaFixaService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DespesaFixaResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaFixaRequestDTO dto) {
        return ResponseEntity.ok(despesaFixaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
        boolean excluido = despesaFixaService.excluir(id);
        if (!excluido) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

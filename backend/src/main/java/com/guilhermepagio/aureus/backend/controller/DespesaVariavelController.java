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

import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.service.DespesaVariavelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/despesas-variaveis")
public class DespesaVariavelController {

    private final DespesaVariavelService despesaVariavelService;

    public DespesaVariavelController(DespesaVariavelService despesaVariavelService) {
        this.despesaVariavelService = despesaVariavelService;
    }

    @GetMapping
    public List<DespesaVariavelResponseDTO> listar() {
        return despesaVariavelService.listar();
    }

    @PostMapping
    public ResponseEntity<DespesaVariavelResponseDTO> criar(final @Valid @RequestBody DespesaVariavelRequestDTO dto) {
        return ResponseEntity.ok(despesaVariavelService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DespesaVariavelResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaVariavelRequestDTO dto) {
        return ResponseEntity.ok(despesaVariavelService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
        boolean excluido = despesaVariavelService.excluir(id);
        if (!excluido) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

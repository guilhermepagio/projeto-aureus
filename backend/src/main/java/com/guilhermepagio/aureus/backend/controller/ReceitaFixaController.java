package com.guilhermepagio.aureus.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
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
    public ResponseEntity<?> criar(final @Valid @RequestBody ReceitaFixaRequestDTO dto) {
        try {
            return ResponseEntity.ok(receitaFixaService.criar(dto));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(final @PathVariable Long id, final @Valid @RequestBody ReceitaFixaRequestDTO dto) {
        try {
            return receitaFixaService.atualizar(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(final @PathVariable Long id) {
        try {
            boolean excluido = receitaFixaService.excluir(id);
            if (!excluido) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (final DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Não é possível excluir esta receita porque ela está em uso."));
        }
    }
}

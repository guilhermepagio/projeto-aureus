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

import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.service.DespesaFixaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<?> criar(final @Valid @RequestBody DespesaFixaRequestDTO dto) {
        try {
            return ResponseEntity.ok(despesaFixaService.criar(dto));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaFixaRequestDTO dto) {
        try {
            return despesaFixaService.atualizar(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(final @PathVariable Long id) {
        try {
            boolean excluido = despesaFixaService.excluir(id);
            if (!excluido) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (final DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Não é possível excluir esta despesa porque ela está em uso."));
        }
    }
}

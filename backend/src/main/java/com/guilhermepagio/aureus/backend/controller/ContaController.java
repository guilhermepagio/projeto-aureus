package com.guilhermepagio.aureus.backend.controller;

import java.util.List;

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

import com.guilhermepagio.aureus.backend.domain.dto.ContaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaResponseDTO;
import com.guilhermepagio.aureus.backend.service.ContaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    public List<ContaResponseDTO> listar() {
        return contaService.listar();
    }

    @PostMapping
    public ContaResponseDTO criar(final @Valid @RequestBody ContaRequestDTO dto) {
        return contaService.criar(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaResponseDTO> atualizar(final @PathVariable Long id, final @Valid @RequestBody ContaRequestDTO dto) {
        return contaService.atualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
        try {
            boolean excluido = contaService.excluir(id);
            if (!excluido) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (final DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

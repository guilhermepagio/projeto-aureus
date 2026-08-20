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

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaRepository contaRepository;

    @GetMapping
    public List<Conta> listar() {
        return contaRepository.findAll();
    }

    @PostMapping
    public Conta criar(final @Valid @RequestBody Conta conta) {
        return contaRepository.save(conta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Conta> atualizar(final @PathVariable Long id, final @Valid @RequestBody Conta contaAtualizada) {
        return contaRepository.findById(id)
                .map(conta -> {
                    conta.setDescricao(contaAtualizada.getDescricao());
                    conta.setObservacoes(contaAtualizada.getObservacoes());
                    return ResponseEntity.ok(contaRepository.save(conta));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
        if (!contaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            contaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (final DataIntegrityViolationException e) {
            // Future-proofing for FK violations (count will be added in Epic 3 when Movimentacao exists)
            return ResponseEntity.badRequest().body(null); // returning empty for now to match current frontend
        }
    }
}

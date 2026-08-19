package com.guilhermepagio.aureus.backend.controller;

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

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
    public Conta criar(@Valid @RequestBody Conta conta) {
        return contaRepository.save(conta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Conta> atualizar(@PathVariable Long id, @Valid @RequestBody Conta contaAtualizada) {
        return contaRepository.findById(id)
                .map(conta -> {
                    conta.setDescricao(contaAtualizada.getDescricao());
                    conta.setObservacoes(contaAtualizada.getObservacoes());
                    return ResponseEntity.ok(contaRepository.save(conta));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!contaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            contaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Future-proofing for FK violations
            return ResponseEntity.badRequest().build();
        }
    }
}

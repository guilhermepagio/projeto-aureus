package com.guilhermepagio.aureus.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/despesas-fixas")
@RequiredArgsConstructor
public class DespesaFixaController {

    private final DespesaFixaRepository repository;

    @GetMapping
    public List<DespesaFixa> listar() {
        return repository.findAll(Sort.by("descricao"));
    }

    @PostMapping
    public ResponseEntity<?> criar(final @Valid @RequestBody DespesaFixa despesaFixa) {
        try {
            despesaFixa.setId(null);
            return ResponseEntity.ok(repository.save(despesaFixa));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaFixa atualizada) {
        return repository.findById(id)
                .map(existente -> {
                    existente.setDescricao(atualizada.getDescricao());
                    existente.setValor(atualizada.getValor());
                    existente.setConta(atualizada.getConta());
                    existente.setCategoria(atualizada.getCategoria());
                    existente.setObservacoes(atualizada.getObservacoes());
                    try {
                        return ResponseEntity.ok(repository.saveAndFlush(existente));
                    } catch (DataIntegrityViolationException e) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(final @PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            repository.flush(); // ensure deletion triggers exception here if violated
            return ResponseEntity.noContent().build();
        } catch (final DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Não é possível excluir esta despesa porque ela está em uso."));
        }
    }
}

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

import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/despesas-variaveis")
@RequiredArgsConstructor
public class DespesaVariavelController {

    private final DespesaVariavelRepository repository;

    @GetMapping
    public List<DespesaVariavel> listar() {
        return repository.findAll(Sort.by("descricao"));
    }

    private void preencherDataFim(DespesaVariavel despesaVariavel) {
        if (despesaVariavel.getDataInicio() != null && despesaVariavel.getQuantidadeParcelas() != null && despesaVariavel.getQuantidadeParcelas() > 0) {
            despesaVariavel.setDataInicio(despesaVariavel.getDataInicio().withDayOfMonth(1));
            despesaVariavel.setDataFim(despesaVariavel.getDataInicio().plusMonths(despesaVariavel.getQuantidadeParcelas() - 1));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(final @Valid @RequestBody DespesaVariavel despesaVariavel) {
        try {
            despesaVariavel.setId(null);
            preencherDataFim(despesaVariavel);
            return ResponseEntity.ok(repository.saveAndFlush(despesaVariavel));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erro de integridade relacional. Verifique os vínculos informados."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(final @PathVariable Long id, final @Valid @RequestBody DespesaVariavel atualizada) {
        return repository.findById(id)
                .map(existente -> {
                    existente.setDescricao(atualizada.getDescricao());
                    existente.setLocalCompra(atualizada.getLocalCompra());
                    existente.setDataCompra(atualizada.getDataCompra());
                    existente.setValorParcela(atualizada.getValorParcela());
                    existente.setQuantidadeParcelas(atualizada.getQuantidadeParcelas());
                    existente.setDataInicio(atualizada.getDataInicio());
                    preencherDataFim(existente);
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
            return ResponseEntity.badRequest().body(Map.of("message", "Erro ao excluir o registro."));
        }
    }
}

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

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @PostMapping
    public Categoria criar(final @Valid @RequestBody Categoria categoria) {
        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(final @PathVariable Long id, final @Valid @RequestBody Categoria categoriaAtualizada) {
        return categoriaRepository.findById(id)
                .map(categoria -> {
                    categoria.setDescricao(categoriaAtualizada.getDescricao());
                    categoria.setObservacoes(categoriaAtualizada.getObservacoes());
                    return ResponseEntity.ok(categoriaRepository.save(categoria));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(final @PathVariable Long id) {
        return categoriaRepository.findById(id).map(categoria -> {
            try {
                categoriaRepository.delete(categoria);
                return ResponseEntity.noContent().<Void>build();
            } catch (final DataIntegrityViolationException e) {
                // Future-proofing for FK violations (count will be added in Epic 3 when Movimentacao exists)
                return ResponseEntity.badRequest().<Void>build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

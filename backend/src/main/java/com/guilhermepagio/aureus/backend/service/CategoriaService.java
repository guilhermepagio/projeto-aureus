package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaResponseDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CategoriaResponseDTO> buscarPorId(Long id) {
        return categoriaRepository.findById(id).map(CategoriaResponseDTO::fromEntity);
    }

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setDescricao(dto.descricao());
        categoria.setObservacoes(dto.observacoes());
        Categoria salva = categoriaRepository.save(categoria);
        return CategoriaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public Optional<CategoriaResponseDTO> atualizar(Long id, CategoriaRequestDTO dto) {
        return categoriaRepository.findById(id)
                .map(categoria -> {
                    categoria.setDescricao(dto.descricao());
                    categoria.setObservacoes(dto.observacoes());
                    return CategoriaResponseDTO.fromEntity(categoriaRepository.save(categoria));
                });
    }

    @Transactional
    public boolean excluir(Long id) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
        if (categoriaOpt.isEmpty()) {
            return false;
        }
        categoriaRepository.delete(categoriaOpt.get());
        categoriaRepository.flush();
        return true;
    }
}

package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaResponseDTO;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CategoriaResponseDTO> buscarPorId(final Long id) {
        return categoriaRepository.findById(id).map(CategoriaResponseDTO::fromEntity);
    }

    @Transactional
    public CategoriaResponseDTO criar(final CategoriaRequestDTO dto) {
        final Categoria categoria = new Categoria();
        categoria.setDescricao(dto.descricao());
        categoria.setObservacoes(dto.observacoes());
        final Categoria salva = categoriaRepository.save(categoria);
        log.info("Operação de cadastro concluída: entidade=Categoria operacao=CRIAR id={}", salva.getId());
        return CategoriaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public CategoriaResponseDTO atualizar(final Long id, final CategoriaRequestDTO dto) {
        final Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));
        categoria.setDescricao(dto.descricao());
        categoria.setObservacoes(dto.observacoes());
        final Categoria salva = categoriaRepository.save(categoria);
        log.info("Operação de cadastro concluída: entidade=Categoria operacao=ATUALIZAR id={}", salva.getId());
        return CategoriaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public boolean excluir(final Long id) {
        if (!categoriaRepository.existsById(id)) {
            return false;
        }
        categoriaRepository.deleteById(id);
        categoriaRepository.flush();
        log.info("Operação de cadastro concluída: entidade=Categoria operacao=EXCLUIR id={}", id);
        return true;
    }
}

package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;

@Service
public class ReceitaFixaService {

    private final ReceitaFixaRepository receitaFixaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    public ReceitaFixaService(ReceitaFixaRepository receitaFixaRepository,
                              ContaRepository contaRepository,
                              CategoriaRepository categoriaRepository) {
        this.receitaFixaRepository = receitaFixaRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ReceitaFixaResponseDTO> listar() {
        return receitaFixaRepository.findAllByOrderByDescricaoAsc().stream()
                .map(ReceitaFixaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ReceitaFixaResponseDTO> buscarPorId(Long id) {
        return receitaFixaRepository.findById(id).map(ReceitaFixaResponseDTO::fromEntity);
    }

    @Transactional
    public ReceitaFixaResponseDTO criar(ReceitaFixaRequestDTO dto) {
        Conta conta = contaRepository.findById(dto.conta().id())
                .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
        Categoria categoria = categoriaRepository.findById(dto.categoria().id())
                .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));

        ReceitaFixa receita = new ReceitaFixa();
        receita.setDescricao(dto.descricao());
        receita.setValor(dto.valor());
        receita.setConta(conta);
        receita.setCategoria(categoria);
        receita.setObservacoes(dto.observacoes());
        receita.setDataInicio(dto.dataInicio());

        ReceitaFixa salva = receitaFixaRepository.saveAndFlush(receita);
        return ReceitaFixaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public Optional<ReceitaFixaResponseDTO> atualizar(Long id, ReceitaFixaRequestDTO dto) {
        return receitaFixaRepository.findById(id)
                .map(existente -> {
                    Conta conta = contaRepository.findById(dto.conta().id())
                            .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
                    Categoria categoria = categoriaRepository.findById(dto.categoria().id())
                            .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));

                    existente.setDescricao(dto.descricao());
                    existente.setValor(dto.valor());
                    existente.setConta(conta);
                    existente.setCategoria(categoria);
                    existente.setObservacoes(dto.observacoes());
                    existente.setDataInicio(dto.dataInicio());

                    ReceitaFixa salva = receitaFixaRepository.saveAndFlush(existente);
                    return ReceitaFixaResponseDTO.fromEntity(salva);
                });
    }

    @Transactional
    public boolean excluir(Long id) {
        if (!receitaFixaRepository.existsById(id)) {
            return false;
        }
        receitaFixaRepository.deleteById(id);
        receitaFixaRepository.flush();
        return true;
    }
}

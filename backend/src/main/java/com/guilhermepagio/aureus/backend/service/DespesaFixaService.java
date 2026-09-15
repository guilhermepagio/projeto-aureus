package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;

@Service
public class DespesaFixaService {

    private final DespesaFixaRepository despesaFixaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    public DespesaFixaService(DespesaFixaRepository despesaFixaRepository,
                              ContaRepository contaRepository,
                              CategoriaRepository categoriaRepository) {
        this.despesaFixaRepository = despesaFixaRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<DespesaFixaResponseDTO> listar() {
        return despesaFixaRepository.findAllByOrderByDescricaoAsc().stream()
                .map(DespesaFixaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DespesaFixaResponseDTO> buscarPorId(Long id) {
        return despesaFixaRepository.findById(id).map(DespesaFixaResponseDTO::fromEntity);
    }

    @Transactional
    public DespesaFixaResponseDTO criar(DespesaFixaRequestDTO dto) {
        Conta conta = contaRepository.findById(dto.conta().id())
                .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
        Categoria categoria = categoriaRepository.findById(dto.categoria().id())
                .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));

        DespesaFixa despesa = new DespesaFixa();
        despesa.setDescricao(dto.descricao());
        despesa.setValor(dto.valor());
        despesa.setConta(conta);
        despesa.setCategoria(categoria);
        despesa.setObservacoes(dto.observacoes());
        despesa.setDataInicio(dto.dataInicio());

        DespesaFixa salva = despesaFixaRepository.saveAndFlush(despesa);
        return DespesaFixaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public Optional<DespesaFixaResponseDTO> atualizar(Long id, DespesaFixaRequestDTO dto) {
        return despesaFixaRepository.findById(id)
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

                    DespesaFixa salva = despesaFixaRepository.saveAndFlush(existente);
                    return DespesaFixaResponseDTO.fromEntity(salva);
                });
    }

    @Transactional
    public boolean excluir(Long id) {
        if (!despesaFixaRepository.existsById(id)) {
            return false;
        }
        despesaFixaRepository.deleteById(id);
        despesaFixaRepository.flush();
        return true;
    }
}

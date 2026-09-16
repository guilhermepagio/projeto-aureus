package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.dto.ContaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaResponseDTO;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;

@Service
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional(readOnly = true)
    public List<ContaResponseDTO> listar() {
        return contaRepository.findAll().stream()
                .map(ContaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ContaResponseDTO> buscarPorId(Long id) {
        return contaRepository.findById(id).map(ContaResponseDTO::fromEntity);
    }

    @Transactional
    public ContaResponseDTO criar(ContaRequestDTO dto) {
        Conta conta = new Conta();
        conta.setDescricao(dto.descricao());
        conta.setObservacoes(dto.observacoes());
        Conta salva = contaRepository.save(conta);
        return ContaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public Optional<ContaResponseDTO> atualizar(Long id, ContaRequestDTO dto) {
        return contaRepository.findById(id)
                .map(conta -> {
                    conta.setDescricao(dto.descricao());
                    conta.setObservacoes(dto.observacoes());
                    return ContaResponseDTO.fromEntity(contaRepository.save(conta));
                });
    }

    @Transactional
    public boolean excluir(Long id) {
        if (!contaRepository.existsById(id)) {
            return false;
        }
        contaRepository.deleteById(id);
        contaRepository.flush();
        return true;
    }
}

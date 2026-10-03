package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.security.TenantContext;

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

    private Conta validarEObterConta(Long contaId) {
        if (contaId == null) {
            throw new ResourceNotFoundException("Conta não encontrada: null");
        }
        String ownerUsuarioId = contaRepository.findOwnerUsuarioId(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
        String currentTenant = TenantContext.getTenantId();
        if (currentTenant == null || !ownerUsuarioId.equals(currentTenant)) {
            throw new AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado");
        }
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
    }

    private Categoria validarEObterCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new ResourceNotFoundException("Categoria não encontrada: null");
        }
        String ownerUsuarioId = categoriaRepository.findOwnerUsuarioId(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
        String currentTenant = TenantContext.getTenantId();
        if (currentTenant == null || !ownerUsuarioId.equals(currentTenant)) {
            throw new AccessDeniedException("Acesso negado: a categoria informada não pertence ao usuário autenticado");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
    }

    @Transactional
    public DespesaFixaResponseDTO criar(DespesaFixaRequestDTO dto) {
        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

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
    public DespesaFixaResponseDTO atualizar(Long id, DespesaFixaRequestDTO dto) {
        DespesaFixa existente = despesaFixaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa fixa não encontrada: " + id));

        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        existente.setDescricao(dto.descricao());
        existente.setValor(dto.valor());
        existente.setConta(conta);
        existente.setCategoria(categoria);
        existente.setObservacoes(dto.observacoes());
        existente.setDataInicio(dto.dataInicio());

        DespesaFixa salva = despesaFixaRepository.saveAndFlush(existente);
        return DespesaFixaResponseDTO.fromEntity(salva);
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

package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaResponseDTO;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.security.TenantContext;

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
    public ReceitaFixaResponseDTO criar(ReceitaFixaRequestDTO dto) {
        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

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
    public ReceitaFixaResponseDTO atualizar(Long id, ReceitaFixaRequestDTO dto) {
        ReceitaFixa existente = receitaFixaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receita fixa não encontrada: " + id));

        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        existente.setDescricao(dto.descricao());
        existente.setValor(dto.valor());
        existente.setConta(conta);
        existente.setCategoria(categoria);
        existente.setObservacoes(dto.observacoes());
        existente.setDataInicio(dto.dataInicio());

        ReceitaFixa salva = receitaFixaRepository.saveAndFlush(existente);
        return ReceitaFixaResponseDTO.fromEntity(salva);
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

package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;
import com.guilhermepagio.aureus.backend.security.TenantContext;

@Service
public class ReceitaVariavelService {

    private final ReceitaVariavelRepository receitaVariavelRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    public ReceitaVariavelService(ReceitaVariavelRepository receitaVariavelRepository,
                                  ContaRepository contaRepository,
                                  CategoriaRepository categoriaRepository) {
        this.receitaVariavelRepository = receitaVariavelRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ReceitaVariavelResponseDTO> listar() {
        return receitaVariavelRepository.findAllByOrderByDescricaoAsc().stream()
                .map(ReceitaVariavelResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ReceitaVariavelResponseDTO> buscarPorId(Long id) {
        return receitaVariavelRepository.findById(id).map(ReceitaVariavelResponseDTO::fromEntity);
    }

    private void preencherDataFim(ReceitaVariavel receitaVariavel) {
        if (receitaVariavel.getDataInicio() != null && receitaVariavel.getQuantidadeParcelas() != null && receitaVariavel.getQuantidadeParcelas() > 0) {
            receitaVariavel.setDataInicio(receitaVariavel.getDataInicio().withDayOfMonth(1));
            receitaVariavel.setDataFim(receitaVariavel.getDataInicio().plusMonths(receitaVariavel.getQuantidadeParcelas() - 1));
        } else {
            receitaVariavel.setDataFim(null);
        }
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
    public ReceitaVariavelResponseDTO criar(ReceitaVariavelRequestDTO dto) {
        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        ReceitaVariavel receita = new ReceitaVariavel();
        receita.setDescricao(dto.descricao());
        receita.setValorParcela(dto.valorParcela());
        receita.setQuantidadeParcelas(dto.quantidadeParcelas());
        receita.setDataInicio(dto.dataInicio());
        preencherDataFim(receita);
        receita.setConta(conta);
        receita.setCategoria(categoria);
        receita.setObservacoes(dto.observacoes());

        ReceitaVariavel salva = receitaVariavelRepository.saveAndFlush(receita);
        return ReceitaVariavelResponseDTO.fromEntity(salva);
    }

    @Transactional
    public ReceitaVariavelResponseDTO atualizar(Long id, ReceitaVariavelRequestDTO dto) {
        ReceitaVariavel existente = receitaVariavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receita variável não encontrada: " + id));

        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        existente.setDescricao(dto.descricao());
        existente.setValorParcela(dto.valorParcela());
        existente.setQuantidadeParcelas(dto.quantidadeParcelas());
        existente.setDataInicio(dto.dataInicio());
        preencherDataFim(existente);
        existente.setConta(conta);
        existente.setCategoria(categoria);
        existente.setObservacoes(dto.observacoes());

        ReceitaVariavel salva = receitaVariavelRepository.saveAndFlush(existente);
        return ReceitaVariavelResponseDTO.fromEntity(salva);
    }

    @Transactional
    public boolean excluir(Long id) {
        if (!receitaVariavelRepository.existsById(id)) {
            return false;
        }
        receitaVariavelRepository.deleteById(id);
        receitaVariavelRepository.flush();
        return true;
    }
}

package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.security.TenantContext;

@Service
public class DespesaVariavelService {

    private final DespesaVariavelRepository despesaVariavelRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    public DespesaVariavelService(DespesaVariavelRepository despesaVariavelRepository,
                                  ContaRepository contaRepository,
                                  CategoriaRepository categoriaRepository) {
        this.despesaVariavelRepository = despesaVariavelRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<DespesaVariavelResponseDTO> listar() {
        return despesaVariavelRepository.findAllByOrderByDescricaoAsc().stream()
                .map(DespesaVariavelResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DespesaVariavelResponseDTO> buscarPorId(Long id) {
        return despesaVariavelRepository.findById(id).map(DespesaVariavelResponseDTO::fromEntity);
    }

    private void preencherDataFim(DespesaVariavel despesaVariavel) {
        if (despesaVariavel.getDataInicio() != null && despesaVariavel.getQuantidadeParcelas() != null && despesaVariavel.getQuantidadeParcelas() > 0) {
            despesaVariavel.setDataInicio(despesaVariavel.getDataInicio().withDayOfMonth(1));
            despesaVariavel.setDataFim(despesaVariavel.getDataInicio().plusMonths(despesaVariavel.getQuantidadeParcelas() - 1));
        } else {
            despesaVariavel.setDataFim(null);
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
    public DespesaVariavelResponseDTO criar(DespesaVariavelRequestDTO dto) {
        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        DespesaVariavel despesa = new DespesaVariavel();
        despesa.setDescricao(dto.descricao());
        despesa.setLocalCompra(dto.localCompra());
        despesa.setDataCompra(dto.dataCompra());
        despesa.setValorParcela(dto.valorParcela());
        despesa.setQuantidadeParcelas(dto.quantidadeParcelas());
        despesa.setDataInicio(dto.dataInicio());
        preencherDataFim(despesa);
        despesa.setConta(conta);
        despesa.setCategoria(categoria);
        despesa.setObservacoes(dto.observacoes());

        DespesaVariavel salva = despesaVariavelRepository.saveAndFlush(despesa);
        return DespesaVariavelResponseDTO.fromEntity(salva);
    }

    @Transactional
    public DespesaVariavelResponseDTO atualizar(Long id, DespesaVariavelRequestDTO dto) {
        DespesaVariavel existente = despesaVariavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa variável não encontrada: " + id));

        Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        existente.setDescricao(dto.descricao());
        existente.setLocalCompra(dto.localCompra());
        existente.setDataCompra(dto.dataCompra());
        existente.setValorParcela(dto.valorParcela());
        existente.setQuantidadeParcelas(dto.quantidadeParcelas());
        existente.setDataInicio(dto.dataInicio());
        preencherDataFim(existente);
        existente.setConta(conta);
        existente.setCategoria(categoria);
        existente.setObservacoes(dto.observacoes());

        DespesaVariavel salva = despesaVariavelRepository.saveAndFlush(existente);
        return DespesaVariavelResponseDTO.fromEntity(salva);
    }

    @Transactional
    public boolean excluir(Long id) {
        if (!despesaVariavelRepository.existsById(id)) {
            return false;
        }
        despesaVariavelRepository.deleteById(id);
        despesaVariavelRepository.flush();
        return true;
    }
}

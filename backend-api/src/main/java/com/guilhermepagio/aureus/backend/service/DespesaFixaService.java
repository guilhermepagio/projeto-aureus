package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DespesaFixaService {

    private static final Logger log = LoggerFactory.getLogger(DespesaFixaService.class);

    private final DespesaFixaRepository despesaFixaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<DespesaFixaResponseDTO> listar() {
        return despesaFixaRepository.findAllByOrderByDescricaoAsc().stream()
                .map(DespesaFixaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DespesaFixaResponseDTO> buscarPorId(final Long id) {
        return despesaFixaRepository.findById(id).map(DespesaFixaResponseDTO::fromEntity);
    }

    private Conta validarEObterConta(final Long contaId) {
        if (contaId == null) {
            throw new ResourceNotFoundException("Conta não encontrada: null");
        }
        final String ownerUsuarioId = contaRepository.findOwnerUsuarioId(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
        final String currentTenant = TenantContext.getTenantId();
        if (currentTenant == null || !ownerUsuarioId.equals(currentTenant)) {
            log.warn("Rejeição de titularidade: entidade=DespesaFixa operacao=VALIDAR_CONTA contaId={} motivo=tenant_incompativel",
                    contaId);
            throw new AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado");
        }
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
    }

    private Categoria validarEObterCategoria(final Long categoriaId) {
        if (categoriaId == null) {
            throw new ResourceNotFoundException("Categoria não encontrada: null");
        }
        final String ownerUsuarioId = categoriaRepository.findOwnerUsuarioId(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
        final String currentTenant = TenantContext.getTenantId();
        if (currentTenant == null || !ownerUsuarioId.equals(currentTenant)) {
            log.warn("Rejeição de titularidade: entidade=DespesaFixa operacao=VALIDAR_CATEGORIA categoriaId={} motivo=tenant_incompativel",
                    categoriaId);
            throw new AccessDeniedException("Acesso negado: a categoria informada não pertence ao usuário autenticado");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
    }

    @Transactional
    public DespesaFixaResponseDTO criar(final DespesaFixaRequestDTO dto) {
        final Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        final Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        final DespesaFixa despesa = new DespesaFixa();
        despesa.setDescricao(dto.descricao());
        despesa.setValor(dto.valor());
        despesa.setConta(conta);
        despesa.setCategoria(categoria);
        despesa.setObservacoes(dto.observacoes());
        despesa.setDataInicio(dto.dataInicio());

        final DespesaFixa salva = despesaFixaRepository.saveAndFlush(despesa);
        log.info("Operação financeira concluída: entidade=DespesaFixa operacao=CRIAR id={} contaId={} categoriaId={}",
                salva.getId(), conta.getId(), categoria.getId());
        return DespesaFixaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public DespesaFixaResponseDTO atualizar(final Long id, final DespesaFixaRequestDTO dto) {
        final DespesaFixa existente = despesaFixaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa fixa não encontrada: " + id));

        final Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        final Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        existente.setDescricao(dto.descricao());
        existente.setValor(dto.valor());
        existente.setConta(conta);
        existente.setCategoria(categoria);
        existente.setObservacoes(dto.observacoes());
        existente.setDataInicio(dto.dataInicio());

        final DespesaFixa salva = despesaFixaRepository.saveAndFlush(existente);
        log.info("Operação financeira concluída: entidade=DespesaFixa operacao=ATUALIZAR id={} contaId={} categoriaId={}",
                salva.getId(), conta.getId(), categoria.getId());
        return DespesaFixaResponseDTO.fromEntity(salva);
    }

    @Transactional
    public boolean excluir(final Long id) {
        if (!despesaFixaRepository.existsById(id)) {
            return false;
        }
        despesaFixaRepository.deleteById(id);
        despesaFixaRepository.flush();
        log.info("Operação financeira concluída: entidade=DespesaFixa operacao=EXCLUIR id={}", id);
        return true;
    }
}

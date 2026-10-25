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
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.security.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DespesaVariavelService {

    private static final Logger log = LoggerFactory.getLogger(DespesaVariavelService.class);

    private final DespesaVariavelRepository despesaVariavelRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<DespesaVariavelResponseDTO> listar() {
        return despesaVariavelRepository.findAllByOrderByDescricaoAsc().stream()
                .map(DespesaVariavelResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DespesaVariavelResponseDTO> buscarPorId(final Long id) {
        return despesaVariavelRepository.findById(id).map(DespesaVariavelResponseDTO::fromEntity);
    }

    private void preencherDataFim(final DespesaVariavel despesaVariavel) {
        if (despesaVariavel.getDataInicio() != null && despesaVariavel.getQuantidadeParcelas() != null && despesaVariavel.getQuantidadeParcelas() > 0) {
            despesaVariavel.setDataInicio(despesaVariavel.getDataInicio().withDayOfMonth(1));
            despesaVariavel.setDataFim(despesaVariavel.getUltimaParcela());
        } else {
            despesaVariavel.setDataFim(null);
        }
    }

    private Conta validarEObterConta(final Long contaId) {
        if (contaId == null) {
            throw new ResourceNotFoundException("Conta não encontrada: null");
        }
        final String ownerUsuarioId = contaRepository.findOwnerUsuarioId(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + contaId));
        final String currentTenant = TenantContext.getTenantId();
        if (currentTenant == null || !ownerUsuarioId.equals(currentTenant)) {
            log.warn("Rejeição de titularidade: entidade=DespesaVariavel operacao=VALIDAR_CONTA contaId={} motivo=tenant_incompativel",
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
            log.warn("Rejeição de titularidade: entidade=DespesaVariavel operacao=VALIDAR_CATEGORIA categoriaId={} motivo=tenant_incompativel",
                    categoriaId);
            throw new AccessDeniedException("Acesso negado: a categoria informada não pertence ao usuário autenticado");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoriaId));
    }

    @Transactional
    public DespesaVariavelResponseDTO criar(final DespesaVariavelRequestDTO dto) {
        final Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        final Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

        final DespesaVariavel despesa = new DespesaVariavel();
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

        final DespesaVariavel salva = despesaVariavelRepository.saveAndFlush(despesa);
        log.info("Operação financeira concluída: entidade=DespesaVariavel operacao=CRIAR id={} contaId={} categoriaId={}",
                salva.getId(), conta.getId(), categoria.getId());
        return DespesaVariavelResponseDTO.fromEntity(salva);
    }

    @Transactional
    public DespesaVariavelResponseDTO atualizar(final Long id, final DespesaVariavelRequestDTO dto) {
        final DespesaVariavel existente = despesaVariavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa variável não encontrada: " + id));

        final Conta conta = validarEObterConta(dto.conta() != null ? dto.conta().id() : null);
        final Categoria categoria = validarEObterCategoria(dto.categoria() != null ? dto.categoria().id() : null);

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

        final DespesaVariavel salva = despesaVariavelRepository.saveAndFlush(existente);
        log.info("Operação financeira concluída: entidade=DespesaVariavel operacao=ATUALIZAR id={} contaId={} categoriaId={}",
                salva.getId(), conta.getId(), categoria.getId());
        return DespesaVariavelResponseDTO.fromEntity(salva);
    }

    @Transactional
    public boolean excluir(final Long id) {
        if (!despesaVariavelRepository.existsById(id)) {
            return false;
        }
        despesaVariavelRepository.deleteById(id);
        despesaVariavelRepository.flush();
        log.info("Operação financeira concluída: entidade=DespesaVariavel operacao=EXCLUIR id={}", id);
        return true;
    }
}

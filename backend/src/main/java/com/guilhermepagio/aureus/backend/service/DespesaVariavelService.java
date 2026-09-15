package com.guilhermepagio.aureus.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelResponseDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;

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
        }
    }

    @Transactional
    public DespesaVariavelResponseDTO criar(DespesaVariavelRequestDTO dto) {
        Conta conta = contaRepository.findById(dto.conta().id())
                .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
        Categoria categoria = categoriaRepository.findById(dto.categoria().id())
                .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));

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
    public Optional<DespesaVariavelResponseDTO> atualizar(Long id, DespesaVariavelRequestDTO dto) {
        return despesaVariavelRepository.findById(id)
                .map(existente -> {
                    Conta conta = contaRepository.findById(dto.conta().id())
                            .orElseThrow(() -> new DataIntegrityViolationException("Conta não encontrada: " + dto.conta().id()));
                    Categoria categoria = categoriaRepository.findById(dto.categoria().id())
                            .orElseThrow(() -> new DataIntegrityViolationException("Categoria não encontrada: " + dto.categoria().id()));

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
                });
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

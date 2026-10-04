package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;

public record DespesaVariavelResponseDTO(
    Long id,
    String descricao,
    String localCompra,
    LocalDate dataCompra,
    BigDecimal valorParcela,
    Integer quantidadeParcelas,
    LocalDate dataInicio,
    LocalDate dataFim,
    ContaResponseDTO conta,
    CategoriaResponseDTO categoria,
    String observacoes
) {
    public static DespesaVariavelResponseDTO fromEntity(DespesaVariavel despesaVariavel) {
        if (despesaVariavel == null) {
            return null;
        }
        return new DespesaVariavelResponseDTO(
            despesaVariavel.getId(),
            despesaVariavel.getDescricao(),
            despesaVariavel.getLocalCompra(),
            despesaVariavel.getDataCompra(),
            despesaVariavel.getValorParcela(),
            despesaVariavel.getQuantidadeParcelas(),
            despesaVariavel.getDataInicio(),
            despesaVariavel.getDataFim(),
            ContaResponseDTO.fromEntity(despesaVariavel.getConta()),
            CategoriaResponseDTO.fromEntity(despesaVariavel.getCategoria()),
            despesaVariavel.getObservacoes()
        );
    }
}

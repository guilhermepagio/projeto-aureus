package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.guilhermepagio.aureus.backend.domain.DespesaFixa;

public record DespesaFixaResponseDTO(
    Long id,
    String descricao,
    BigDecimal valor,
    ContaResponseDTO conta,
    CategoriaResponseDTO categoria,
    String observacoes,
    LocalDate dataInicio
) {
    public static DespesaFixaResponseDTO fromEntity(DespesaFixa despesaFixa) {
        if (despesaFixa == null) {
            return null;
        }
        return new DespesaFixaResponseDTO(
            despesaFixa.getId(),
            despesaFixa.getDescricao(),
            despesaFixa.getValor(),
            ContaResponseDTO.fromEntity(despesaFixa.getConta()),
            CategoriaResponseDTO.fromEntity(despesaFixa.getCategoria()),
            despesaFixa.getObservacoes(),
            despesaFixa.getDataInicio()
        );
    }
}

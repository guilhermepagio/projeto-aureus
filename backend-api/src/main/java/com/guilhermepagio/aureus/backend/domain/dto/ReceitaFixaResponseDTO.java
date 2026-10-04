package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;

public record ReceitaFixaResponseDTO(
    Long id,
    String descricao,
    BigDecimal valor,
    ContaResponseDTO conta,
    CategoriaResponseDTO categoria,
    String observacoes,
    LocalDate dataInicio
) {
    public static ReceitaFixaResponseDTO fromEntity(ReceitaFixa receitaFixa) {
        if (receitaFixa == null) {
            return null;
        }
        return new ReceitaFixaResponseDTO(
            receitaFixa.getId(),
            receitaFixa.getDescricao(),
            receitaFixa.getValor(),
            ContaResponseDTO.fromEntity(receitaFixa.getConta()),
            CategoriaResponseDTO.fromEntity(receitaFixa.getCategoria()),
            receitaFixa.getObservacoes(),
            receitaFixa.getDataInicio()
        );
    }
}

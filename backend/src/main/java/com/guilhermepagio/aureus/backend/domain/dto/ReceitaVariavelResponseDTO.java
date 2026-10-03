package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;

public record ReceitaVariavelResponseDTO(
    Long id,
    String descricao,
    BigDecimal valorParcela,
    Integer quantidadeParcelas,
    LocalDate dataInicio,
    LocalDate dataFim,
    ContaResponseDTO conta,
    CategoriaResponseDTO categoria,
    String observacoes
) {
    public static ReceitaVariavelResponseDTO fromEntity(ReceitaVariavel receitaVariavel) {
        if (receitaVariavel == null) {
            return null;
        }
        return new ReceitaVariavelResponseDTO(
            receitaVariavel.getId(),
            receitaVariavel.getDescricao(),
            receitaVariavel.getValorParcela(),
            receitaVariavel.getQuantidadeParcelas(),
            receitaVariavel.getDataInicio(),
            receitaVariavel.getDataFim(),
            ContaResponseDTO.fromEntity(receitaVariavel.getConta()),
            CategoriaResponseDTO.fromEntity(receitaVariavel.getCategoria()),
            receitaVariavel.getObservacoes()
        );
    }
}

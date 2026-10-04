package com.guilhermepagio.aureus.backend.domain.dto;

import com.guilhermepagio.aureus.backend.domain.Conta;

public record ContaResponseDTO(
    Long id,
    String descricao,
    String observacoes
) {
    public static ContaResponseDTO fromEntity(Conta conta) {
        if (conta == null) {
            return null;
        }
        return new ContaResponseDTO(conta.getId(), conta.getDescricao(), conta.getObservacoes());
    }
}

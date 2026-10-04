package com.guilhermepagio.aureus.backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 20, message = "A descrição deve ter no máximo 20 caracteres")
    String descricao,

    @Size(max = 300, message = "As observações devem ter no máximo 300 caracteres")
    String observacoes
) {}

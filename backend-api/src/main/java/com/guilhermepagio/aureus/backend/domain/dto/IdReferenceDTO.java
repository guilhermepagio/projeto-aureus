package com.guilhermepagio.aureus.backend.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IdReferenceDTO(
    @NotNull(message = "O ID é obrigatório")
    @Positive(message = "O ID deve ser maior que zero")
    Long id
) {}

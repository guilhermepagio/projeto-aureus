package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DespesaFixaRequestDTO(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    String descricao,

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser maior que zero")
    @DecimalMax(value = "9999999.99", message = "O valor deve ser de no máximo R$ 9.999.999,99")
    @Digits(integer = 7, fraction = 2, message = "Formato numérico inválido")
    BigDecimal valor,

    @NotNull(message = "Selecione uma conta")
    @Valid
    IdReferenceDTO conta,

    @NotNull(message = "Selecione uma categoria")
    @Valid
    IdReferenceDTO categoria,

    @Size(max = 300, message = "As observações devem ter no máximo 300 caracteres")
    String observacoes,

    LocalDate dataInicio
) {}

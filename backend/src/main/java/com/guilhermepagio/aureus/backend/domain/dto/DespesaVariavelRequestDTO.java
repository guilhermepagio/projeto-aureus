package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DespesaVariavelRequestDTO(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    String descricao,

    @Size(max = 100, message = "O local da compra deve ter no máximo 100 caracteres")
    String localCompra,

    LocalDate dataCompra,

    @NotNull(message = "O valor da parcela é obrigatório")
    @Positive(message = "O valor da parcela deve ser maior que zero")
    @DecimalMax(value = "9999999.99", message = "O valor deve ser de no máximo R$ 9.999.999,99")
    @Digits(integer = 7, fraction = 2, message = "Formato numérico inválido")
    BigDecimal valorParcela,

    @NotNull(message = "A quantidade de parcelas é obrigatória")
    @Min(value = 1, message = "A quantidade de parcelas deve ser pelo menos 1")
    @Max(value = 1200, message = "Máximo de 1200 parcelas")
    Integer quantidadeParcelas,

    @NotNull(message = "A data de início é obrigatória")
    LocalDate dataInicio,

    @NotNull(message = "Selecione uma conta")
    @Valid
    IdReferenceDTO conta,

    @NotNull(message = "Selecione uma categoria")
    @Valid
    IdReferenceDTO categoria,

    @Size(max = 300, message = "As observações devem ter no máximo 300 caracteres")
    String observacoes
) {}

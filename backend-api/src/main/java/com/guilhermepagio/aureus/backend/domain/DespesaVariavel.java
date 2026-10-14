package com.guilhermepagio.aureus.backend.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Transient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "despesas_variaveis", indexes = {
    @Index(name = "idx_despesas_variaveis_usuario_datas", columnList = "usuario_id, data_inicio, data_fim"),
    @Index(name = "idx_despesas_variaveis_datas", columnList = "data_inicio, data_fim")
})
@NoArgsConstructor
@AllArgsConstructor
public class DespesaVariavel extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    @Getter
    @Setter
    private String descricao;

    @Size(max = 100, message = "O local da compra deve ter no máximo 100 caracteres")
    @Column(length = 100)
    @Getter
    @Setter
    private String localCompra;

    @Column
    @Getter
    @Setter
    private LocalDate dataCompra;

    @NotNull(message = "O valor da parcela é obrigatório")
    @Positive(message = "O valor da parcela deve ser maior que zero")
    @DecimalMax(value = "9999999.99", message = "O valor deve ser de no máximo R$ 9.999.999,99")
    @Digits(integer = 7, fraction = 2, message = "Formato numérico inválido")
    @Column(nullable = false, precision = 9, scale = 2)
    @Getter
    @Setter
    private BigDecimal valorParcela;

    @NotNull(message = "A quantidade de parcelas é obrigatória")
    @Min(value = 1, message = "A quantidade de parcelas deve ser pelo menos 1")
    @Max(value = 1200, message = "Máximo de 1200 parcelas")
    @Column(nullable = false)
    @Getter
    @Setter
    private Integer quantidadeParcelas;

    @NotNull(message = "A data de início é obrigatória")
    @Column(nullable = false)
    @Getter
    @Setter
    private LocalDate dataInicio;

    @Column(nullable = false)
    @Getter
    @Setter
    private LocalDate dataFim;

    @NotNull(message = "Selecione uma categoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Getter
    @Setter
    private Categoria categoria;

    @NotNull(message = "Selecione uma conta")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Getter
    @Setter
    private Conta conta;

    @Size(max = 300, message = "As observações devem ter no máximo 300 caracteres")
    @Column(length = 300)
    @Getter
    @Setter
    private String observacoes;

    /**
     * Calcula o valor total do parcelamento (valorParcela * quantidadeParcelas).
     * Retorna 0.00 se qualquer campo for nulo ou quantidade for não-positiva.
     */
    @Transient
    @JsonIgnore
    public BigDecimal getValorTotal() {
        if (valorParcela == null || quantidadeParcelas == null || quantidadeParcelas <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valorParcela.multiply(BigDecimal.valueOf(quantidadeParcelas)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a data correspondente à última parcela (mês final projetado no dia 1).
     * Retorna null se dataInicio ou quantidadeParcelas forem inválidos.
     */
    @Transient
    @JsonIgnore
    public LocalDate getUltimaParcela() {
        if (dataInicio == null || quantidadeParcelas == null || quantidadeParcelas <= 0) {
            return null;
        }
        return dataInicio.withDayOfMonth(1).plusMonths(quantidadeParcelas - 1);
    }
}

package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConsolidacaoPorContaDTO {
    
    private List<LinhaConsolidacaoDTO> receitas;
    private List<LinhaConsolidacaoDTO> despesas;
    private BigDecimal saldoHistoricoPreGrade = BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);

    public ConsolidacaoPorContaDTO(List<LinhaConsolidacaoDTO> receitas, List<LinhaConsolidacaoDTO> despesas) {
        this(receitas, despesas, BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    public ConsolidacaoPorContaDTO(List<LinhaConsolidacaoDTO> receitas, List<LinhaConsolidacaoDTO> despesas, BigDecimal saldoHistoricoPreGrade) {
        this.receitas = receitas;
        this.despesas = despesas;
        this.saldoHistoricoPreGrade = saldoHistoricoPreGrade != null 
                ? saldoHistoricoPreGrade.setScale(2, java.math.RoundingMode.HALF_UP) 
                : BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public void setSaldoHistoricoPreGrade(BigDecimal saldoHistoricoPreGrade) {
        this.saldoHistoricoPreGrade = saldoHistoricoPreGrade != null 
                ? saldoHistoricoPreGrade.setScale(2, java.math.RoundingMode.HALF_UP) 
                : BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinhaConsolidacaoDTO {
        private Long contaId;
        private String contaDescricao;
        private List<BigDecimal> valoresMensais;
    }
}

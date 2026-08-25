package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidacaoPorContaDTO {
    
    private List<LinhaConsolidacaoDTO> receitas;
    private List<LinhaConsolidacaoDTO> despesas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinhaConsolidacaoDTO {
        private Long contaId;
        private String contaDescricao;
        private List<BigDecimal> valoresMensais;
    }
}

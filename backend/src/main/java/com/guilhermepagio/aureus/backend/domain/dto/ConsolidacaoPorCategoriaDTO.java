package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidacaoPorCategoriaDTO {
    
    private List<LinhaConsolidacaoCategoriaDTO> despesas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinhaConsolidacaoCategoriaDTO {
        private Long categoriaId;
        private String categoriaDescricao;
        private List<BigDecimal> valoresMensais;
    }
}

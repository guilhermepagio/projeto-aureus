package com.guilhermepagio.aureus.backend.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public class ConsolidacaoPorCategoriaDTO {
    
    private List<LinhaConsolidacaoCategoriaDTO> receitas;
    private List<LinhaConsolidacaoCategoriaDTO> despesas;

    public ConsolidacaoPorCategoriaDTO() {}

    public ConsolidacaoPorCategoriaDTO(List<LinhaConsolidacaoCategoriaDTO> receitas, List<LinhaConsolidacaoCategoriaDTO> despesas) {
        this.receitas = receitas;
        this.despesas = despesas;
    }

    public List<LinhaConsolidacaoCategoriaDTO> getReceitas() {
        return receitas;
    }

    public void setReceitas(List<LinhaConsolidacaoCategoriaDTO> receitas) {
        this.receitas = receitas;
    }

    public List<LinhaConsolidacaoCategoriaDTO> getDespesas() {
        return despesas;
    }

    public void setDespesas(List<LinhaConsolidacaoCategoriaDTO> despesas) {
        this.despesas = despesas;
    }

    public static class LinhaConsolidacaoCategoriaDTO {
        private Long categoriaId;
        private String categoriaDescricao;
        private List<BigDecimal> valoresMensais;

        public LinhaConsolidacaoCategoriaDTO() {}

        public LinhaConsolidacaoCategoriaDTO(Long categoriaId, String categoriaDescricao, List<BigDecimal> valoresMensais) {
            this.categoriaId = categoriaId;
            this.categoriaDescricao = categoriaDescricao;
            this.valoresMensais = valoresMensais;
        }

        public Long getCategoriaId() {
            return categoriaId;
        }

        public void setCategoriaId(Long categoriaId) {
            this.categoriaId = categoriaId;
        }

        public String getCategoriaDescricao() {
            return categoriaDescricao;
        }

        public void setCategoriaDescricao(String categoriaDescricao) {
            this.categoriaDescricao = categoriaDescricao;
        }

        public List<BigDecimal> getValoresMensais() {
            return valoresMensais;
        }

        public void setValoresMensais(List<BigDecimal> valoresMensais) {
            this.valoresMensais = valoresMensais;
        }
    }
}

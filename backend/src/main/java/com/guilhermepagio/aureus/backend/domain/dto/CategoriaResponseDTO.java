package com.guilhermepagio.aureus.backend.domain.dto;

import com.guilhermepagio.aureus.backend.domain.Categoria;

public record CategoriaResponseDTO(
    Long id,
    String descricao,
    String observacoes
) {
    public static CategoriaResponseDTO fromEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponseDTO(categoria.getId(), categoria.getDescricao(), categoria.getObservacoes());
    }
}

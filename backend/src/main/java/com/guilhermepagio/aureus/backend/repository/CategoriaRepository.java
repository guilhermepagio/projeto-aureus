package com.guilhermepagio.aureus.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.guilhermepagio.aureus.backend.domain.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioIdOrderByDescricaoAsc(String usuarioId);

    @Query(value = "SELECT usuario_id FROM categorias WHERE id = :id", nativeQuery = true)
    Optional<String> findOwnerUsuarioId(@Param("id") Long id);
}

package com.guilhermepagio.aureus.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.guilhermepagio.aureus.backend.domain.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioId(String usuarioId);
}

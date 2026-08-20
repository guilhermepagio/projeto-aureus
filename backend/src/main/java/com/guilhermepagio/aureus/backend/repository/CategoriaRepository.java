package com.guilhermepagio.aureus.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guilhermepagio.aureus.backend.domain.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}

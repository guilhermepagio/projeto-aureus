package com.guilhermepagio.aureus.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;

public interface ReceitaVariavelRepository extends JpaRepository<ReceitaVariavel, Long> {
    List<ReceitaVariavel> findByUsuarioId(String usuarioId);
}

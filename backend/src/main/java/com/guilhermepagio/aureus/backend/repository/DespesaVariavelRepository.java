package com.guilhermepagio.aureus.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;

public interface DespesaVariavelRepository extends JpaRepository<DespesaVariavel, Long> {
    List<DespesaVariavel> findByUsuarioId(String usuarioId);
}

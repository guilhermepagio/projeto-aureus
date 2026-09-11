package com.guilhermepagio.aureus.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;

public interface ReceitaFixaRepository extends JpaRepository<ReceitaFixa, Long> {
    List<ReceitaFixa> findByUsuarioId(String usuarioId);
}

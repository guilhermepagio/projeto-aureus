package com.guilhermepagio.aureus.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.guilhermepagio.aureus.backend.domain.DespesaFixa;

public interface DespesaFixaRepository extends JpaRepository<DespesaFixa, Long> {
    List<DespesaFixa> findByUsuarioId(String usuarioId);
}

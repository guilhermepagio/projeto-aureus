package com.guilhermepagio.aureus.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.guilhermepagio.aureus.backend.domain.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    List<Conta> findByUsuarioId(String usuarioId);
}

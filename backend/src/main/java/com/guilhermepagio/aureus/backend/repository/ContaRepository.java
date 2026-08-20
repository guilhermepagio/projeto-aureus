package com.guilhermepagio.aureus.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guilhermepagio.aureus.backend.domain.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {
}

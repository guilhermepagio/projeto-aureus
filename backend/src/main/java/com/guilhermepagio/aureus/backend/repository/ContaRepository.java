package com.guilhermepagio.aureus.backend.repository;

import com.guilhermepagio.aureus.backend.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {
}

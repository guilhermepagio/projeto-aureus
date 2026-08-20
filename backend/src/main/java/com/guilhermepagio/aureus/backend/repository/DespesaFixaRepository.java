package com.guilhermepagio.aureus.backend.repository;

import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DespesaFixaRepository extends JpaRepository<DespesaFixa, Long> {
}

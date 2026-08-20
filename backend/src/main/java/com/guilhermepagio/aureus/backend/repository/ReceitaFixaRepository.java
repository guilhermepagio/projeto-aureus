package com.guilhermepagio.aureus.backend.repository;

import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceitaFixaRepository extends JpaRepository<ReceitaFixa, Long> {
}

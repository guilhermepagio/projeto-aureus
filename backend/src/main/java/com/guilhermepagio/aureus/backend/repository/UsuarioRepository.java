package com.guilhermepagio.aureus.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guilhermepagio.aureus.backend.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByGoogleSubjectId(String googleSubjectId);
}

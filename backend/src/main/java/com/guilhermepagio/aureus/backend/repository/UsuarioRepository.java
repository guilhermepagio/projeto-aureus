package com.guilhermepagio.aureus.backend.repository;

import com.guilhermepagio.aureus.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByGoogleSubjectId(String googleSubjectId);
}

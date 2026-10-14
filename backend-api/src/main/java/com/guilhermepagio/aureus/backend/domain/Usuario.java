package com.guilhermepagio.aureus.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_usuarios_google_subject_id", columnList = "google_subject_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(unique = true, nullable = false)
    @Getter
    @Setter
    private String googleSubjectId;

    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String nome;
    @Column(length = 1024)
    @Getter
    @Setter
    private String fotoPerfil;
}

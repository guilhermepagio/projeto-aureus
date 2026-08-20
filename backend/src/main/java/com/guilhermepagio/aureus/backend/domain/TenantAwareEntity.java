package com.guilhermepagio.aureus.backend.domain;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class TenantAwareEntity {

    @TenantId
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private String usuarioId;
}

package com.guilhermepagio.aureus.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.TenantId;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
public abstract class TenantAwareEntity {

    @TenantId
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private String usuarioId;
}

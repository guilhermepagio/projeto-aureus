package com.guilhermepagio.aureus.backend.domain;

import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.TenantId;

@MappedSuperclass
public abstract class TenantAwareEntity {

    @TenantId
    private String usuarioId;

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}

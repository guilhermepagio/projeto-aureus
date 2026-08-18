package com.guilhermepagio.aureus.backend.security;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : "__NO_TENANT__";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

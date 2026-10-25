package com.guilhermepagio.aureus.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CurrentTenantIdentifierResolverImplTest {

    private CurrentTenantIdentifierResolverImpl resolver;

    @BeforeEach
    void setUp() {
        resolver = new CurrentTenantIdentifierResolverImpl();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve retornar o identificador do tenant quando TenantContext estiver preenchido")
    void deveRetornarTenantIdQuandoContextoEstiverPreenchido() {
        TenantContext.setTenantId("user-42");

        final String resolved = resolver.resolveCurrentTenantIdentifier();

        assertThat(resolved).isEqualTo("user-42");
    }

    @Test
    @DisplayName("Deve retornar fallback public quando TenantContext for nulo")
    void deveRetornarPublicQuandoContextoForNulo() {
        TenantContext.clear();

        final String resolved = resolver.resolveCurrentTenantIdentifier();

        assertThat(resolved).isEqualTo("public");
    }

    @Test
    @DisplayName("Deve retornar fallback public quando TenantContext for string vazia")
    void deveRetornarPublicQuandoContextoForStringVazia() {
        TenantContext.setTenantId("");

        final String resolved = resolver.resolveCurrentTenantIdentifier();

        assertThat(resolved).isEqualTo("public");
    }

    @Test
    @DisplayName("Deve retornar fallback public quando TenantContext contiver apenas espaços em branco")
    void deveRetornarPublicQuandoContextoContiverApenasEspacosEmBranco() {
        TenantContext.setTenantId("   ");
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo("public");

        TenantContext.setTenantId("\t \n");
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo("public");
    }

    @Test
    @DisplayName("Deve retornar true em validateExistingCurrentSessions")
    void deveRetornarTrueEmValidateExistingCurrentSessions() {
        assertThat(resolver.validateExistingCurrentSessions()).isTrue();
    }
}

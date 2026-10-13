package com.guilhermepagio.aureus.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import com.guilhermepagio.aureus.backend.security.JwtUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BackendApplicationIT extends AbstractIntegrationTest {

    private final Flyway flyway;

    BackendApplicationIT(
            final MockMvc mockMvc,
            final JwtUtil jwtUtil,
            @Value("${jwt.secret}") final String jwtSecret,
            final Flyway flyway) {
        super(mockMvc, jwtUtil, jwtSecret);
        this.flyway = flyway;
    }

    @Test
    void contextLoads() {
        assertNotNull(flyway, "O bean do Flyway deve ser carregado e configurado no contexto");
        assertNotNull(flyway.info().current(), "As migrações Flyway devem estar aplicadas");
        assertTrue(flyway.info().current().getState().isApplied(), "O estado da migração deve ser aplicado");
        assertEquals(0, flyway.info().pending().length, "Não devem existir migrações pendentes");
    }

}

package com.guilhermepagio.aureus.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendApplicationIT extends AbstractIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void contextLoads() {
        assertNotNull(flyway, "O bean do Flyway deve ser carregado e configurado no contexto");
        assertNotNull(flyway.info().current(), "As migrações Flyway devem estar aplicadas");
        assertTrue(flyway.info().current().getState().isApplied(), "O estado da migração deve ser aplicado");
        assertEquals(0, flyway.info().pending().length, "Não devem existir migrações pendentes");
    }

}

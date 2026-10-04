package com.guilhermepagio.aureus.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BackendApplicationTests {

    private final Flyway flyway;

    BackendApplicationTests(Flyway flyway) {
        this.flyway = flyway;
    }

	@Test
	void contextLoads() {
        assertNotNull(flyway, "O bean do Flyway deve ser carregado e configurado no contexto");
	}

}

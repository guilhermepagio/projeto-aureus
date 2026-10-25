package com.guilhermepagio.aureus.backend.investigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

class InvestigationEvidenceTest {
    @TempDir Path temporary;

    @Test void publicaJsonValidadoComUmUnicoArtefato() throws Exception {
        var destination = temporary.resolve("measurement.json");
        Files.writeString(destination, "{\"anterior\":true}");
        byte[] next = "{\"nova\":true}".getBytes(StandardCharsets.UTF_8);
        InvestigationEvidence.publish(destination, next,
                staged -> assertThat(Files.readAllBytes(staged)).isEqualTo(next));
        assertThat(Files.readAllBytes(destination)).isEqualTo(next);
        try (var files = Files.list(temporary)) {
            assertThat(files.map(path -> path.getFileName().toString()).toList()).containsExactly("measurement.json");
        }
    }

    @Test void falhaDepoisDeEstagiarAntesDoCommitPreservaBytesAnteriores() throws Exception {
        var destination = temporary.resolve("measurement.json");
        byte[] previous = "{\"anterior\":true}".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, previous);
        assertThrows(IOException.class, () -> InvestigationEvidence.publish(destination,
                "{\"nova\":true}".getBytes(StandardCharsets.UTF_8), staged -> {
                    assertThat(Files.readString(staged)).contains("nova");
                    throw new IOException("Falha sintética pré-commit");
                }));
        assertThat(Files.readAllBytes(destination)).isEqualTo(previous);
        try (var files = Files.list(temporary)) {
            assertThat(files.map(path -> path.getFileName().toString()).toList()).containsExactly("measurement.json");
        }
    }

    @Test void jsonInvalidoNaoSubstituiEvidenciaAnterior() throws Exception {
        var destination = temporary.resolve("measurement.json");
        byte[] previous = "{\"anterior\":true}".getBytes(StandardCharsets.UTF_8);
        Files.write(destination, previous);
        assertThrows(IOException.class, () -> InvestigationEvidence.publish(destination, new byte[] { '{' }, staged -> {}));
        assertThat(Files.readAllBytes(destination)).isEqualTo(previous);
    }

    @Test void execucoesSeparadasNaoCompletamUmaAOutraEColetaIncompletaFalha() throws Exception {
        var destination = temporary.resolve("measurement.json");
        var environment = Map.<String, Object>of("gitRevision", "baseline", "sha256", Map.of());
        var first = new InvestigationEvidence.Run(destination);
        first.complete("security", Map.of("marker", "run-anterior"), environment);
        assertThrows(IllegalStateException.class, first::close);
        var second = new InvestigationEvidence.Run(destination);
        second.complete("performance", Map.of(), environment);
        assertThrows(IllegalStateException.class, second::close);
        assertThat(Files.exists(destination)).isFalse();
        assertThrows(IllegalStateException.class, () -> first.complete("performance", Map.of(), environment));
        var third = new InvestigationEvidence.Run(destination);
        assertThat(third.id()).isNotIn(first.id(), second.id());
        third.complete("security", Map.of("marker", "run-nova"), environment);
        third.complete("performance", Map.of(), environment);
        third.close();
        var json = new ObjectMapper().readTree(Files.readAllBytes(destination));
        assertThat(json.get("runId").asText()).isEqualTo(third.id());
        assertThat(json.get("security").get("marker").asText()).isEqualTo("run-nova");
        assertThat(Files.readString(destination)).doesNotContain("run-anterior");
    }

    @Test void falhaDeTesteDescartaColetaMesmoComAsDuasSecoes() throws Exception {
        var destination = temporary.resolve("measurement.json");
        var environment = Map.<String, Object>of("gitRevision", "baseline", "sha256", Map.of());
        var run = new InvestigationEvidence.Run(destination);
        run.complete("security", Map.of(), environment);
        run.complete("performance", Map.of(), environment);
        run.failed();
        assertThrows(IllegalStateException.class, run::close);
        assertThat(Files.exists(destination)).isFalse();
    }

    @Test void rejeitaForksSeparadosOuJvmNaoReutilizada() {
        InvestigationEvidence.requireSingleReusedJvm("1", "true");
        assertThrows(IllegalStateException.class, () -> InvestigationEvidence.requireSingleReusedJvm("2", "true"));
        assertThrows(IllegalStateException.class, () -> InvestigationEvidence.requireSingleReusedJvm("1", "false"));
    }
}

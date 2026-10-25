package com.guilhermepagio.aureus.backend.investigation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Publicação e ciclo de vida limitados à execução JUnit; sem estado agregado estático. */
final class InvestigationEvidence {
    private InvestigationEvidence() {}

    @FunctionalInterface interface PreCommit { void validate(Path staged) throws IOException; }

    static void publish(Path destination, byte[] bytes, PreCommit validation) throws IOException {
        Files.createDirectories(destination.getParent());
        Path staged = Files.createTempFile(destination.getParent(), "measurement-", ".pending");
        try {
            Files.write(staged, bytes);
            new ObjectMapper().readTree(Files.readAllBytes(staged));
            validation.validate(staged);
            // Único commit. Nenhuma operação de I/O ou validação ocorre depois dele.
            Files.move(staged, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException | Error failure) {
            try {
                Files.deleteIfExists(staged);
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    static void requireSingleReusedJvm(String forks, String reuse) {
        if (!"1".equals(forks) || !"true".equalsIgnoreCase(reuse)) {
            throw new IllegalStateException("Coleta 7.5 exige -DforkCount=1 -DreuseForks=true; forks separados não podem agregar as duas suítes");
        }
    }

    static final class Run implements ExtensionContext.Store.CloseableResource {
        private final String id = UUID.randomUUID().toString();
        private final String startedAt = Instant.now().toString();
        private final Path destination;
        private final Map<String, Object> sections = new LinkedHashMap<>();
        private Map<String, Object> metadata;
        private boolean failed;
        private boolean closed;

        Run(Path destination) { this.destination = destination; }
        String id() { return id; }

        synchronized void complete(String section, Object evidence, Map<String, Object> environment) {
            if (closed) throw new IllegalStateException("Execução encerrada; suas seções foram descartadas");
            if (!Set.of("security", "performance").contains(section) || sections.containsKey(section)) {
                failed = true;
                throw new IllegalStateException("Seção investigativa inválida ou duplicada: " + section);
            }
            if (metadata != null && (!metadata.get("gitRevision").equals(environment.get("gitRevision"))
                    || !metadata.get("sha256").equals(environment.get("sha256")))) {
                failed = true;
                throw new IllegalStateException("As fontes mudaram entre as duas suítes; coleta descartada");
            }
            metadata = new LinkedHashMap<>(environment);
            sections.put(section, evidence);
        }

        synchronized void failed() { failed = true; }

        @Override public synchronized void close() throws IOException {
            if (closed) return;
            closed = true;
            byte[] bytes;
            try {
                if (failed || !sections.keySet().equals(Set.of("security", "performance"))) {
                    throw new IllegalStateException("Coleta 7.5 incompleta ou com falha; nenhuma evidência publicada. "
                            + "Execute ambas as suítes na mesma JVM reutilizada. Seções concluídas: " + sections.keySet());
                }
                var data = new LinkedHashMap<>(metadata);
                data.put("schemaVersion", 2);
                data.put("runId", id);
                data.put("startedAt", startedAt);
                data.put("completedAt", Instant.now().toString());
                data.put("execution", Map.of("stateOwner", "JUnit root ExtensionContext.Store", "forkCount", 1,
                        "reuseForks", true, "publication", destination.toString().replace('\\', '/')));
                data.putAll(sections);
                bytes = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
                String content = new String(bytes, StandardCharsets.UTF_8);
                if (content.contains("eyJ") || content.contains("AUREUS_SESSION=")) {
                    throw new IllegalStateException("Credencial detectada; evidência descartada antes da publicação");
                }
            } finally {
                // Descartar antes do commit; o Store não pode reaproveitar seções em outro lançamento.
                sections.clear();
                metadata = null;
            }
            publish(destination, bytes, staged -> {});
        }
    }

    public static final class RunExtension implements BeforeAllCallback, ParameterResolver, TestWatcher {
        private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(InvestigationEvidence.class);

        private Run run(ExtensionContext context) {
            return context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent("run", key -> {
                Path root = Path.of("..").toAbsolutePath().normalize();
                String directory = Boolean.getBoolean("investigation.publish")
                        ? "_bmad-output/implementation-artifacts/7-5-investigation-evidence"
                        : "backend-api/target/7-5-investigation-evidence";
                return new Run(root.resolve(directory).resolve("measurement.json"));
            }, Run.class);
        }

        @Override public void beforeAll(ExtensionContext context) {
            requireSingleReusedJvm(System.getProperty("forkCount", "1"), System.getProperty("reuseForks", "true"));
            run(context);
        }

        @Override public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
            return parameter.getParameter().getType() == Run.class;
        }

        @Override public Object resolveParameter(ParameterContext parameter, ExtensionContext context) { return run(context); }
        @Override public void testFailed(ExtensionContext context, Throwable cause) { run(context).failed(); }
        @Override public void testAborted(ExtensionContext context, Throwable cause) { run(context).failed(); }
        @Override public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) { run(context).failed(); }
    }
}

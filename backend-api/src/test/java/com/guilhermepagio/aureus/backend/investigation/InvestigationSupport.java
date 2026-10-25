package com.guilhermepagio.aureus.backend.investigation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.DriverManager;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.guilhermepagio.aureus.backend.AbstractIntegrationTest;
import com.guilhermepagio.aureus.backend.security.JwtUtil;
import com.guilhermepagio.aureus.backend.security.TenantContext;
import com.sun.management.ThreadMXBean;

import jakarta.persistence.EntityManagerFactory;

/** Instrumentação exclusivamente de teste; nunca registra binds, cookies ou JWTs. */
@Import(InvestigationSupport.Instrumentation.class)
@ExtendWith(InvestigationEvidence.RunExtension.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.session.events.log=false"
})
abstract class InvestigationSupport extends AbstractIntegrationTest {
    static final List<String> TABLES = List.of("despesas_fixas", "receitas_fixas", "despesas_variaveis",
            "receitas_variaveis", "contas", "categorias");
    static final List<String> TENANTS = List.of("invest-a", "invest-b", "public");
    private static final String DATABASE = "investigation75";

    static {
        // O contêiner do AbstractIntegrationTest é reutilizado, mas nunca seu banco.
        try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(),
                postgres.getPassword()); var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + DATABASE);
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected EntityManagerFactory emf;
    @Autowired protected PlatformTransactionManager transactionManager;

    InvestigationSupport(MockMvc mockMvc, JwtUtil jwtUtil, @Value("${jwt.secret}") String jwtSecret) {
        super(mockMvc, jwtUtil, jwtSecret);
    }

    protected SessionFactoryImplementor factory() {
        return emf.unwrap(SessionFactoryImplementor.class);
    }

    protected <T> T tenant(String tenant, Supplier<T> action) {
        TenantContext.setTenantId(tenant);
        try {
            return action.get();
        } finally {
            TenantContext.clear();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    protected <T> T transaction(String tenant, Supplier<T> action) {
        return tenant(tenant, () -> new TransactionTemplate(transactionManager).execute(status -> action.get()));
    }

    protected void cleanDatabase() {
        assertThat(jdbc.queryForObject("SELECT current_database()", String.class)).isEqualTo(DATABASE);
        jdbc.execute("TRUNCATE despesas_fixas,receitas_fixas,despesas_variaveis,receitas_variaveis,contas,categorias,usuarios RESTART IDENTITY CASCADE");
        emf.getCache().evictAll();
    }

    protected Map<String, List<Map<String, Object>>> financialSnapshot() {
        var snapshot = new LinkedHashMap<String, List<Map<String, Object>>>();
        for (String table : TABLES) snapshot.put(table, jdbc.queryForList("SELECT * FROM " + table + " ORDER BY id"));
        return snapshot;
    }

    protected void complete(String section, Object evidence, InvestigationEvidence.Run run) throws Exception {
        var root = Path.of("..").toAbsolutePath().normalize();
        var data = new LinkedHashMap<String, Object>();
        data.put("gitRevision", command(root, "git", "rev-parse", "HEAD"));
        data.put("workingTreeStatus", command(root, "git", "status", "--porcelain=v1"));
        data.put("gitDiffSha256", sha256(command(root, "git", "diff", "--binary").getBytes(StandardCharsets.UTF_8)));
        data.put("java", System.getProperty("java.runtime.version"));
        data.put("vm", System.getProperty("java.vm.name"));
        data.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        data.put("processors", Runtime.getRuntime().availableProcessors());
        data.put("timezone", java.time.ZoneId.systemDefault().getId());
        data.put("maxHeapBytes", Runtime.getRuntime().maxMemory());
        data.put("postgresVersion", jdbc.queryForObject("SELECT version()", String.class));
        data.put("postgresSettings", jdbc.queryForList("SELECT name,setting,unit FROM pg_settings WHERE name IN "
                + "('shared_buffers','work_mem','effective_cache_size','max_connections','timezone') ORDER BY name"));
        data.put("dockerEngineVersion", postgres.getDockerClient().versionCmd().exec().getVersion());
        var container = postgres.getContainerInfo();
        data.put("postgresImage", container.getConfig().getImage());
        data.put("postgresImageId", container.getImageId());
        data.put("postgresRepoDigests", postgres.getDockerClient().inspectImageCmd(container.getImageId()).exec().getRepoDigests());
        data.put("database", DATABASE);
        data.put("measurementBoundaries", Map.of("included", "MockMvc síncrono ou serviço, sessão/transação, serialização e instrumentação SQL (regex/mapas)",
                "excluded", "Fixture, emissão do cookie, validação e coleta de métricas",
                "heap", "Usado antes/depois na JVM; não é pico", "allocation", "Bytes alocados pela thread de execução",
                "cold", "Primeira execução da operação/carga; caches de disco e JVM não são reiniciados",
                "instrumentationOverhead", "Não quantificado; tempo/alocação incluem regex/mapas proporcionais às consultas",
                "ownershipFraction", "Razão entre medianas separadas de titularidade e criação revertida; não compara criação persistida nem decompõe causalmente o tempo"));
        data.put("commands", List.of("$env:JAVA_HOME='C:/Program Files/Java/jdk-21'",
                "./mvnw.cmd verify -Pintegration-test -DforkCount=1 -DreuseForks=true \"-Dit.test=SecurityInvestigationIT,PerformanceInvestigationIT\"",
                "./mvnw.cmd verify -Pintegration-test -DforkCount=1 -DreuseForks=true \"-Dit.test=SecurityInvestigationIT,PerformanceInvestigationIT\" -Dinvestigation.publish=true",
                "./mvnw.cmd verify -Pintegration-test -DforkCount=1 -DreuseForks=true", "git diff --check"));
        var hashes = new TreeMap<String, String>();
        for (var directory : List.of("backend-api/src/main", "backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation")) {
            try (var files = Files.walk(root.resolve(directory))) {
                for (var file : files.filter(Files::isRegularFile).sorted().toList()) {
                    hashes.put(root.relativize(file).toString().replace('\\', '/'), sha256(Files.readAllBytes(file)));
                }
            }
        }
        for (String file : List.of("backend-api/pom.xml",
                "backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java",
                "frontend-web/src/components/Header/Header.tsx", "frontend-web/src/components/Header/Header.test.tsx",
                "_bmad-output/specs/spec-epico-7/seguranca.md", "_bmad-output/specs/spec-epico-7/desempenho.md",
                "_bmad-output/specs/spec-epico-7/rastreabilidade.md")) {
            hashes.put(file, sha256(Files.readAllBytes(root.resolve(file))));
        }
        data.put("sha256", hashes);
        data.put("checks", List.of("Duas suítes concluídas sem falha", "Banco PostgreSQL descartável próprio",
                "Sem parâmetros SQL, JWTs ou cookies", "Oráculo financeiro independente em centavos",
                "Uma fria, três aquecimentos, dez amostras e sessões frescas por operação"));
        run.complete(section, evidence, data);
    }

    static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static String command(Path directory, String... arguments) throws IOException, InterruptedException {
        var process = new ProcessBuilder(arguments).directory(directory.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (process.waitFor() != 0) throw new IOException("Falha de metadados: " + arguments[0]);
        return output;
    }

    protected Sample sample(CheckedSupplier<Payload> action) throws Exception {
        emf.getCache().evictAll();
        var stats = factory().getStatistics();
        stats.clear();
        SqlTrace.begin();
        var bean = ManagementFactory.getPlatformMXBean(ThreadMXBean.class);
        assertThat(bean.isThreadAllocatedMemorySupported()).isTrue();
        bean.setThreadAllocatedMemoryEnabled(true);
        long thread = Thread.currentThread().threadId();
        long heapBefore = usedHeap();
        long allocatedBefore = bean.getThreadAllocatedBytes(thread);
        long start = System.nanoTime();
        Payload payload;
        try {
            payload = action.get();
        } catch (Exception | Error exception) {
            SqlTrace.end();
            throw exception;
        }
        long elapsed = System.nanoTime() - start;
        long allocated = bean.getThreadAllocatedBytes(thread) - allocatedBefore;
        long heapAfter = usedHeap();
        var sql = SqlTrace.end();
        assertThat(stats.getSessionOpenCount()).as("Sessão fresca por amostra").isPositive();
        assertThat(stats.getSessionCloseCount()).isEqualTo(stats.getSessionOpenCount());
        assertThat(sql.values().stream().mapToLong(Integer::longValue).sum()).isEqualTo(stats.getPrepareStatementCount());
        return new Sample(elapsed, payload.body().length, stats.getPrepareStatementCount(), stats.getEntityLoadCount(),
                stats.getSessionOpenCount(), stats.getSessionCloseCount(), allocated, heapBefore, heapAfter, sql, payload);
    }

    private static long usedHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @FunctionalInterface interface CheckedSupplier<T> { T get() throws Exception; }
    record Payload(int status, byte[] body) {}
    record Sample(long elapsedNs, long responseBytes, long preparedStatements, long entitiesLoaded,
                  long sessionsOpened, long sessionsClosed,
                  long allocatedBytes, long heapBeforeBytes, long heapAfterBytes,
                  Map<String, Integer> normalizedSql, @com.fasterxml.jackson.annotation.JsonIgnore Payload payload) {}

    @TestConfiguration(proxyBeanMethods = false)
    static class Instrumentation {
        @Bean DynamicPropertyRegistrar investigationDatabase() {
            // Registrar é aplicado após o DynamicPropertySource herdado do teste base.
            return registry -> registry.add("spring.datasource.url", () -> postgres.getJdbcUrl().replace("/test", "/" + DATABASE));
        }
        @Bean HibernatePropertiesCustomizer investigationSqlInspector() {
            return properties -> properties.put("hibernate.session_factory.statement_inspector", new SqlTrace());
        }
    }

    static class SqlTrace implements StatementInspector {
        private static final ThreadLocal<Map<String, Integer>> SQL = new ThreadLocal<>();
        static void begin() { SQL.set(new LinkedHashMap<>()); }
        static Map<String, Integer> end() {
            var result = SQL.get();
            SQL.remove();
            return result == null ? Map.of() : result;
        }
        @Override public String inspect(String sql) {
            if (SQL.get() != null) {
                // Literais removidos mesmo em SQL nativo; os binds nunca são observados.
                String normalized = sql.replaceAll("'([^']|'')*'", "?").replaceAll("\\b\\d+\\b", "?")
                        .replaceAll("\\s+", " ").trim();
                SQL.get().merge(normalized, 1, Integer::sum);
            }
            return sql;
        }
    }
}

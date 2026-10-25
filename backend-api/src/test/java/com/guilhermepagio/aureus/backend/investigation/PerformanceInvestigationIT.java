package com.guilhermepagio.aureus.backend.investigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.security.JwtUtil;
import com.guilhermepagio.aureus.backend.service.DespesaVariavelService;

/** Carga sintética determinística; nenhuma otimização ou migração de produção. */
public class PerformanceInvestigationIT extends InvestigationSupport {
    private static final YearMonth FIRST = YearMonth.of(2026, 1);
    private static final List<String> PATHS = List.of("contas", "categorias", "despesas-fixas", "receitas-fixas",
            "despesas-variaveis", "receitas-variaveis");
    @Autowired private ContaRepository contas;
    @Autowired private CategoriaRepository categorias;
    @Autowired private DespesaVariavelService despesas;
    private final Map<String, jakarta.servlet.http.Cookie> cookies = new HashMap<>();

    PerformanceInvestigationIT(MockMvc mockMvc, JwtUtil jwtUtil, @Value("${jwt.secret}") String jwtSecret) {
        super(mockMvc, jwtUtil, jwtSecret);
    }

    @Test
    void medeCargasListagensLazyETitularidadeComOraculoIndependente(InvestigationEvidence.Run run) throws Exception {
        var measurements = new ArrayList<Map<String, Object>>();
        var distributions = new ArrayList<Map<String, Object>>();
        Map<String, Object> evidence;
        try {
            for (String tenant : List.of("invest-a", "invest-b", "public", "invest-empty")) cookies.put(tenant, authCookie(tenant));
            for (int n : new int[] { 100, 1000, 10000 }) {
                int references = n / 10;
                var oracles = seed(n, references, false);
                distributions.add(distribution(n, references));
                // Primeiro as consolidações, incluindo resumo e histórico no retorno por conta.
                measure(measurements, "consolidacao-conta", n, references, "distribuidas",
                        () -> http("consolidacao/por-conta?mesAno=2026-01", "invest-a"),
                        payload -> validateAccounts(payload, oracles.get(0)));
                measure(measurements, "consolidacao-categoria", n, references, "distribuidas",
                        () -> http("consolidacao/por-categoria?mesAno=2026-01", "invest-a"),
                        payload -> validateCategories(payload, oracles.get(0)));
                for (String path : PATHS) {
                    int expected = path.equals("contas") || path.equals("categorias") ? references : n;
                    measure(measurements, "listagem-" + path, n, references, "distribuidas",
                            () -> http(path, "invest-a"), payload -> validateList(payload, expected, "invest-a", references));
                }
                // Validações de isolamento e tenant vazio ficam fora do intervalo medido.
                for (int tenant = 1; tenant < TENANTS.size(); tenant++) {
                    validateAccounts(http("consolidacao/por-conta?mesAno=2026-01", TENANTS.get(tenant)), oracles.get(tenant));
                    validateCategories(http("consolidacao/por-categoria?mesAno=2026-01", TENANTS.get(tenant)), oracles.get(tenant));
                    for (String path : PATHS) validateList(http(path, TENANTS.get(tenant)),
                            path.equals("contas") || path.equals("categorias") ? references : n, TENANTS.get(tenant), references);
                }
                validateEmptyTenant();
            }
            // Experimento N+1 controlado: mesmos N movimentos, variando somente os pares efetivamente usados.
            for (int n : new int[] { 10, 100, 1000 }) {
                for (boolean shared : new boolean[] { true, false }) {
                    seed(n, n, shared);
                    measure(measurements, "dto-lazy-despesas-variaveis", n, n, shared ? "compartilhadas" : "distintas",
                            () -> http("despesas-variaveis", "invest-a"), payload -> validateList(payload, n, "invest-a", n));
                    var last = measurements.getLast();
                    long sql = ((Number) last.get("medianPreparedStatements")).longValue();
                    assertThat(sql).as("Uma listagem e uma consulta por conta/categoria efetivamente lida")
                            .isEqualTo(1L + 2L * (shared ? 1 : n));
                }
            }
            // Titularidade tem prioridade menor e é medida por último.
            seed(100, 10, false);
            var request = expenseRequest(100001L, 100001L);
            int pairIndex = measurements.size();
            measure(measurements, "titularidade-conta-categoria", 100, 10, "proprias", () -> {
                return transaction("invest-a", () -> {
                    if (!contas.findOwnerUsuarioId(100001L).orElseThrow().equals("invest-a")) throw new IllegalStateException("Titularidade divergente");
                    contas.findById(100001L).orElseThrow();
                    if (!categorias.findOwnerUsuarioId(100001L).orElseThrow().equals("invest-a")) throw new IllegalStateException("Titularidade divergente");
                    categorias.findById(100001L).orElseThrow();
                    return new Payload(200, new byte[0]);
                });
            }, payload -> assertThat(payload.status()).isEqualTo(200));
            measure(measurements, "criacao-revertida-despesa-variavel", 100, 10, "proprias", () -> {
                // Rollback é parte da operação medida; fixture e cardinalidade não se alteram.
                return tenant("invest-a", () -> new org.springframework.transaction.support.TransactionTemplate(transactionManager)
                        .execute(status -> {
                            var created = despesas.criar(request);
                            status.setRollbackOnly();
                            try {
                                return new Payload(200, objectMapper.writeValueAsBytes(created));
                            } catch (Exception exception) {
                                throw new IllegalStateException(exception);
                            }
                        }));
            }, payload -> assertThat(payload.status()).isEqualTo(200));
            double pairNs = ((Number) measurements.get(pairIndex).get("medianElapsedNs")).doubleValue();
            double completeNs = ((Number) measurements.getLast().get("medianElapsedNs")).doubleValue();
            measurements.get(pairIndex).put("fractionOfRolledBackCreationMedian", pairNs / completeNs);
            validateContracts();
            evidence = Map.of("window", "2026-01/2027-12", "tenants", TENANTS,
                    "fixtureDistributions", distributions, "measurements", measurements,
                    "checks", List.of("24 meses por linha e totais mensais em centavos", "Saldo histórico anterior a 2026 preservado",
                            "Seletores completos em A/B/public e tenant vazio", "DTOs incluem descrição e observações das relações",
                            "403/404 e tentativas rejeitadas sem mutação", "N+1 compartilhado versus distinto confirmado"));
        } finally {
            com.guilhermepagio.aureus.backend.security.TenantContext.clear();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            cleanDatabase();
        }
        complete("performance", evidence, run);
    }

    private Payload http(String path, String tenant) throws Exception {
        var response = mockMvc.perform(get("/api/" + path).cookie(cookies.get(tenant))).andReturn().getResponse();
        return new Payload(response.getStatus(), response.getContentAsByteArray());
    }

    private void measure(List<Map<String, Object>> output, String operation, int n, int refs, String relations,
                         CheckedSupplier<Payload> action, Consumer<Payload> validate) throws Exception {
        var cold = sample(action);
        validate.accept(cold.payload());
        for (int i = 0; i < 3; i++) validate.accept(sample(action).payload());
        var samples = new ArrayList<Sample>();
        for (int i = 0; i < 10; i++) {
            var value = sample(action);
            validate.accept(value.payload());
            samples.add(value);
        }
        var row = new LinkedHashMap<String, Object>();
        row.put("operation", operation);
        row.put("movementsPerFamilyPerTenant", n);
        row.put("accountsAndCategoriesPerTenant", refs);
        row.put("relations", relations);
        row.put("tenant", "invest-a");
        row.put("cold", cold);
        row.put("warmupCount", 3);
        row.put("samples", samples);
        row.put("medianElapsedNs", median(samples.stream().mapToLong(Sample::elapsedNs).toArray()));
        row.put("maxElapsedNs", samples.stream().mapToLong(Sample::elapsedNs).max().orElseThrow());
        row.put("medianPreparedStatements", median(samples.stream().mapToLong(Sample::preparedStatements).toArray()));
        row.put("maxPreparedStatements", samples.stream().mapToLong(Sample::preparedStatements).max().orElseThrow());
        row.put("medianEntitiesLoaded", median(samples.stream().mapToLong(Sample::entitiesLoaded).toArray()));
        row.put("maxEntitiesLoaded", samples.stream().mapToLong(Sample::entitiesLoaded).max().orElseThrow());
        row.put("medianAllocatedBytes", median(samples.stream().mapToLong(Sample::allocatedBytes).toArray()));
        row.put("maxAllocatedBytes", samples.stream().mapToLong(Sample::allocatedBytes).max().orElseThrow());
        row.put("maxResponseBytes", samples.stream().mapToLong(Sample::responseBytes).max().orElseThrow());
        row.put("medianResponseBytes", median(samples.stream().mapToLong(Sample::responseBytes).toArray()));
        row.put("medianObservedHeapBytes", median(samples.stream().mapToLong(Sample::heapAfterBytes).toArray()));
        row.put("maxObservedHeapBytes", samples.stream().mapToLong(Sample::heapAfterBytes).max().orElseThrow());
        row.put("exploratorySignals", Map.of("medianAtLeast200ms", ((Number) row.get("medianElapsedNs")).longValue() >= 200_000_000,
                "responseAtLeast1MiB", ((Number) row.get("maxResponseBytes")).longValue() >= 1_048_576,
                "allocationAtLeast32MiB", ((Number) row.get("medianAllocatedBytes")).longValue() >= 33_554_432));
        output.add(row);
        System.out.println("Investigação 7.5: " + operation + " N=" + n + " relações=" + relations
                + " medianaMs=" + ((Number) row.get("medianElapsedNs")).doubleValue() / 1_000_000
                + " SQL=" + row.get("medianPreparedStatements"));
    }

    private static double median(long[] values) {
        Arrays.sort(values);
        return (values[values.length / 2 - 1] / 2.0) + (values[values.length / 2] / 2.0);
    }

    private List<Oracle> seed(int n, int refs, boolean shared) {
        cleanDatabase();
        var oracles = new ArrayList<Oracle>();
        for (int t = 0; t < TENANTS.size(); t++) {
            String tenant = TENANTS.get(t);
            long base = (t + 1L) * 100000;
            for (String table : List.of("contas", "categorias")) {
                jdbc.update("INSERT INTO " + table + "(id,usuario_id,descricao,observacoes) "
                        + "SELECT ?+i,?,?||i,?||i FROM generate_series(1,?) i", base, tenant, "Ref ", tenant + " observação ", refs);
            }
            var oracle = new Oracle();
            for (String table : TABLES.subList(0, 4)) {
                boolean fixed = table.endsWith("fixas");
                boolean income = table.startsWith("receitas");
                var arguments = new ArrayList<Object[]>();
                for (int i = 1; i <= n; i++) {
                    YearMonth start = i == 1 ? YearMonth.of(2025, 12) : i == 2 ? YearMonth.of(2027, 12)
                            : YearMonth.of(2021, 1).plusMonths((i - 1) % 96);
                    LocalDate date = fixed && i % 17 == 0 ? null : start.atDay(1);
                    int installments = (int) Math.min(i <= 2 ? 3 : i % 4 == 0 ? 36 : i % 4 + 1,
                            ChronoUnit.MONTHS.between(start, YearMonth.of(2028, 12)) + 1);
                    long cents = (income ? fixed ? 2001 + i % 97 : 701 + i % 79 : fixed ? 1001 + i % 89 : 301 + i % 67) * (t + 1L);
                    long account = base + (shared ? 1 : (i - 1) % refs + 1);
                    long category = base + (shared ? 1 : ((i - 1) * 7) % refs + 1);
                    String observation = i % 3 == 0 ? null : "Movimento sintético " + tenant;
                    if (fixed) arguments.add(new Object[] { base + i, tenant, "Mov " + i, BigDecimal.valueOf(cents, 2),
                            date, account, category, observation });
                    else arguments.add(new Object[] { base + i, tenant, "Mov " + i, BigDecimal.valueOf(cents, 2),
                            installments, date, start.plusMonths(installments - 1).atDay(1), account, category, observation });
                    oracle.add(income, fixed, date == null ? null : start, installments, cents, account, category);
                }
                String columns = fixed ? "valor,data_inicio" : "valor_parcela,quantidade_parcelas,data_inicio,data_fim";
                jdbc.batchUpdate("INSERT INTO " + table + "(id,usuario_id,descricao," + columns
                        + ",conta_id,categoria_id,observacoes) VALUES (" + (fixed ? "?,?,?,?,?,?,?,?" : "?,?,?,?,?,?,?,?,?,?") + ")", arguments);
            }
            oracles.add(oracle);
        }
        for (String table : TABLES) {
            jdbc.execute("SELECT setval(pg_get_serial_sequence('" + table + "','id'), (SELECT max(id) FROM " + table + "))");
        }
        jdbc.execute("ANALYZE");
        return oracles;
    }

    private Map<String, Object> distribution(int n, int refs) {
        var result = new LinkedHashMap<String, Object>();
        result.put("movementsPerFamilyPerTenant", n);
        result.put("referencesPerTypePerTenant", refs);
        result.put("totalMovementRows", n * 4 * 3);
        result.put("byTenantAndFamily", jdbc.queryForList("SELECT usuario_id, 'despesas_fixas' familia, count(*) linhas, "
                + "count(*) FILTER (WHERE data_inicio < DATE '2026-01-01') anteriores, "
                + "count(*) FILTER (WHERE data_inicio >= DATE '2028-01-01') posteriores, "
                + "count(*) FILTER (WHERE data_inicio IS NULL) sem_data FROM despesas_fixas GROUP BY usuario_id "
                + "UNION ALL SELECT usuario_id,'receitas_fixas',count(*),count(*) FILTER (WHERE data_inicio < DATE '2026-01-01'),"
                + "count(*) FILTER (WHERE data_inicio >= DATE '2028-01-01'),count(*) FILTER (WHERE data_inicio IS NULL) FROM receitas_fixas GROUP BY usuario_id "
                + "UNION ALL SELECT usuario_id,'despesas_variaveis',count(*),count(*) FILTER (WHERE data_inicio < DATE '2026-01-01'),"
                + "count(*) FILTER (WHERE data_inicio >= DATE '2028-01-01'),0 FROM despesas_variaveis GROUP BY usuario_id "
                + "UNION ALL SELECT usuario_id,'receitas_variaveis',count(*),count(*) FILTER (WHERE data_inicio < DATE '2026-01-01'),"
                + "count(*) FILTER (WHERE data_inicio >= DATE '2028-01-01'),0 FROM receitas_variaveis GROUP BY usuario_id"));
        return result;
    }

    private JsonNode json(Payload payload) {
        assertThat(payload.status()).isEqualTo(200);
        try {
            return objectMapper.readTree(payload.body());
        } catch (Exception exception) {
            throw new AssertionError("JSON inválido", exception);
        }
    }

    private void validateAccounts(Payload payload, Oracle oracle) {
        var body = json(payload);
        validateLines(body.get("receitas"), "contaId", oracle.income);
        validateLines(body.get("despesas"), "contaId", oracle.expense);
        assertThat(body.get("saldoHistoricoPreGrade").decimalValue()).isEqualByComparingTo(BigDecimal.valueOf(oracle.history, 2));
    }

    private void validateCategories(Payload payload, Oracle oracle) {
        validateLines(json(payload).get("despesas"), "categoriaId", oracle.categories);
    }

    private void validateLines(JsonNode lines, String idField, Map<Long, long[]> expected) {
        long expectedNonzero = expected.values().stream().filter(values -> Arrays.stream(values).anyMatch(value -> value != 0)).count();
        assertThat(lines.size()).isEqualTo(expectedNonzero);
        Set<Long> expectedIds = expected.entrySet().stream()
                .filter(entry -> Arrays.stream(entry.getValue()).anyMatch(value -> value != 0))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        var actualIds = new ArrayList<Long>();
        long[] totals = new long[24];
        for (JsonNode line : lines) {
            actualIds.add(line.get(idField).asLong());
            long[] values = expected.get(line.get(idField).asLong());
            assertThat(values).isNotNull();
            assertThat(line.get("valoresMensais").size()).isEqualTo(24);
            for (int month = 0; month < 24; month++) {
                BigDecimal actual = line.get("valoresMensais").get(month).decimalValue();
                assertThat(actual).isEqualByComparingTo(BigDecimal.valueOf(values[month], 2));
                totals[month] += actual.movePointRight(2).longValueExact();
            }
        }
        assertThat(actualIds).doesNotHaveDuplicates().containsExactlyInAnyOrderElementsOf(expectedIds);
        for (int month = 0; month < 24; month++) {
            long total = 0;
            for (long[] values : expected.values()) total += values[month];
            assertThat(totals[month]).isEqualTo(total);
        }
    }

    private void validateList(Payload payload, int expected, String tenant, int references) {
        var body = json(payload);
        assertThat(body.isArray()).isTrue();
        assertThat(body.size()).isEqualTo(expected);
        long base = (TENANTS.indexOf(tenant) + 1L) * 100000;
        Set<Long> expectedIds = LongStream.rangeClosed(base + 1, base + expected).boxed().collect(Collectors.toSet());
        var actualIds = new ArrayList<Long>();
        for (var row : body) {
            actualIds.add(row.get("id").asLong());
            assertThat(row.get("id").asLong()).isBetween(base + 1, base + Math.max(expected, references));
            if (row.has("conta")) {
                for (String relation : List.of("conta", "categoria")) {
                    assertThat(row.get(relation).get("id").asLong()).isBetween(base + 1, base + references);
                    assertThat(row.get(relation).get("descricao").asText()).startsWith("Ref ");
                    assertThat(row.get(relation).get("observacoes").asText()).startsWith(tenant + " observação ");
                }
            } else {
                assertThat(row.get("observacoes").asText()).startsWith(tenant + " observação ");
            }
        }
        assertThat(actualIds).doesNotHaveDuplicates().containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    private void validateEmptyTenant() throws Exception {
        for (String path : PATHS) assertThat(json(http(path, "invest-empty")).size()).isZero();
        var account = json(http("consolidacao/por-conta?mesAno=2026-01", "invest-empty"));
        assertThat(account.get("receitas").size()).isZero();
        assertThat(account.get("despesas").size()).isZero();
        assertThat(account.get("saldoHistoricoPreGrade").decimalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(json(http("consolidacao/por-categoria?mesAno=2026-01", "invest-empty")).get("despesas").size()).isZero();
    }

    private DespesaVariavelRequestDTO expenseRequest(Long account, Long category) {
        return new DespesaVariavelRequestDTO("Sintética", null, null, new BigDecimal("13.37"), 3,
                LocalDate.of(2025, 12, 1), new IdReferenceDTO(account), new IdReferenceDTO(category), null);
    }

    private void validateContracts() throws Exception {
        var before = financialSnapshot();
        for (var request : List.of(expenseRequest(200001L, 100001L), expenseRequest(100001L, 200001L),
                expenseRequest(99999999L, 100001L))) {
            int expected = request.conta().id().equals(99999999L) ? 404 : 403;
            var response = mockMvc.perform(post("/api/despesas-variaveis").cookie(authCookie("invest-a"))
                    .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
                    .andReturn().getResponse();
            assertThat(response.getStatus()).isEqualTo(expected);
        }
        for (var builder : List.of(put("/api/despesas-variaveis/200001")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(expenseRequest(100001L, 100001L))),
                delete("/api/despesas-variaveis/200001"))) {
            assertThat(mockMvc.perform(builder.cookie(authCookie("invest-a")).with(csrf()))
                    .andReturn().getResponse().getStatus()).isEqualTo(404);
        }
        assertThat(mockMvc.perform(get("/api/contas/200001").cookie(authCookie("invest-a")))
                .andReturn().getResponse().getStatus()).isEqualTo(404);
        assertThat(financialSnapshot()).as("Rejeições não alteram nenhuma das seis tabelas financeiras").isEqualTo(before);
    }

    /** Oráculo em centavos: enumera cada competência, sem chamar serviços/DTOs de produção. */
    private static class Oracle {
        final Map<Long, long[]> income = new LinkedHashMap<>();
        final Map<Long, long[]> expense = new LinkedHashMap<>();
        final Map<Long, long[]> categories = new LinkedHashMap<>();
        long history;

        void add(boolean isIncome, boolean fixed, YearMonth start, int installments, long cents, long account, long category) {
            YearMonth cursor = start == null ? FIRST : start;
            YearMonth last = fixed ? FIRST.plusMonths(23) : start.plusMonths(installments - 1);
            while (!cursor.isAfter(last)) {
                if (cursor.isBefore(FIRST)) history += isIncome ? cents : -cents;
                else if (cursor.isBefore(FIRST.plusMonths(24))) {
                    int index = (int) ChronoUnit.MONTHS.between(FIRST, cursor);
                    (isIncome ? income : expense).computeIfAbsent(account, ignored -> new long[24])[index] += cents;
                    if (!isIncome) categories.computeIfAbsent(category, ignored -> new long[24])[index] += cents;
                }
                cursor = cursor.plusMonths(1);
            }
        }
    }
}

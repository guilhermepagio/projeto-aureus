package com.guilhermepagio.aureus.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.guilhermepagio.aureus.backend.BackendApplication;

/** Banco próprio e um esquema por cenário: não altera a suíte HTTP compartilhada. */
@Testcontainers
class IndexMigrationIT {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    private static final List<String> MOVIMENTACOES = List.of(
            "despesas_fixas", "receitas_fixas", "despesas_variaveis", "receitas_variaveis");

    private String schema;
    private String url;

    @BeforeEach
    void criarEsquemaIsolado() throws SQLException {
        schema = "indices_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
                var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
        }
        url = POSTGRES.getJdbcUrl() + (POSTGRES.getJdbcUrl().contains("?") ? "&" : "?")
                + "currentSchema=" + schema;
    }

    @AfterEach
    void removerSomenteEsquemaDoCenario() throws SQLException {
        try (var connection = conectar(); var statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    @Test
    void baseLimpaRecebeV2SemDuplicarIndicesEReaplicaSemPendencias() throws SQLException {
        var flyway = flyway(null);
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
        verificarCatalogoTenant();
        verificarIntegridade();
        assertThat(indiceExiste("idx_usuarios_google_subject_id")).isFalse();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    void upgradePreservaHistoricoDadosEUniqueAposBootstrapJpa() throws SQLException {
        var v1 = flyway("1");
        v1.migrate();
        var checksumV1 = v1.info().current().getChecksum();
        var fksV1 = catalogoFks();
        executar("INSERT INTO usuarios(google_subject_id, nome) VALUES ('subject-original', 'Original')");
        inserirDadosFinanceiros();
        verificarDadosFinanceiros();

        assertThat(flyway(null).migrate().migrationsExecuted).isEqualTo(1);
        verificarDadosFinanceiros();
        verificarIntegridade();
        verificarFksPreservadas(fksV1);

        // Inicializa o aplicativo real com ddl-auto=update, depois do Flyway.
        try (var context = new SpringApplicationBuilder(BackendApplication.class)
                .web(WebApplicationType.SERVLET)
                .run("--server.port=0", "--spring.datasource.url=" + url,
                        "--spring.datasource.username=" + POSTGRES.getUsername(),
                        "--spring.datasource.password=" + POSTGRES.getPassword(),
                        "--spring.flyway.schemas=" + schema,
                        "--spring.flyway.default-schema=" + schema,
                        "--spring.jpa.hibernate.ddl-auto=update")) {
            assertThat(context.getBean(Flyway.class).info().pending()).isEmpty();
            verificarCatalogoTenant();
            assertThat(indiceExiste("idx_usuarios_google_subject_id")).isFalse();
            verificarIntegridade();
            verificarDadosFinanceiros();
            verificarFksPreservadas(fksV1);
        }
        var migrations = flyway(null).info().applied();
        assertThat(migrations).hasSize(2);
        assertThat(migrations[0].getChecksum()).isEqualTo(checksumV1);
        assertThat(flyway(null).validateWithResult().validationSuccessful).isTrue();
        assertThat(texto("SELECT nome FROM usuarios WHERE google_subject_id='subject-original'"))
                .isEqualTo("Original");
    }

    static Stream<Arguments> catalogosNaoRedundantes() {
        return Stream.of(
                Arguments.of("ausente", "DROP INDEX idx_usuarios_google_subject_id", false),
                Arguments.of("outra coluna", "DROP INDEX idx_usuarios_google_subject_id; "
                        + "CREATE INDEX idx_usuarios_google_subject_id ON usuarios(email)", true),
                Arguments.of("parcial", "DROP INDEX idx_usuarios_google_subject_id; "
                        + "CREATE INDEX idx_usuarios_google_subject_id ON usuarios(google_subject_id) WHERE email IS NOT NULL", true),
                Arguments.of("include", "DROP INDEX idx_usuarios_google_subject_id; "
                        + "CREATE INDEX idx_usuarios_google_subject_id ON usuarios(google_subject_id) INCLUDE(email)", true),
                Arguments.of("constraint", "DROP INDEX idx_usuarios_google_subject_id; "
                        + "ALTER TABLE usuarios ADD CONSTRAINT idx_usuarios_google_subject_id UNIQUE(google_subject_id)", true),
                Arguments.of("unique ausente", "ALTER TABLE usuarios DROP CONSTRAINT usuarios_google_subject_id_key", true),
                Arguments.of("unique invalida", "UPDATE pg_index SET indisvalid=false "
                        + "WHERE indexrelid='usuarios_google_subject_id_key'::regclass", true),
                Arguments.of("secundario invalido", "UPDATE pg_index SET indisvalid=false "
                        + "WHERE indexrelid='idx_usuarios_google_subject_id'::regclass", true),
                Arguments.of("opclass diferente", "DROP INDEX idx_usuarios_google_subject_id; "
                        + "CREATE INDEX idx_usuarios_google_subject_id ON usuarios(google_subject_id varchar_pattern_ops)", true),
                Arguments.of("expressao", "DROP INDEX idx_usuarios_google_subject_id; "
                        + "CREATE INDEX idx_usuarios_google_subject_id ON usuarios(lower(google_subject_id))", true),
                Arguments.of("collation diferente", "DROP INDEX idx_usuarios_google_subject_id; "
                        + "CREATE INDEX idx_usuarios_google_subject_id ON usuarios(google_subject_id COLLATE \"C\")", true),
                Arguments.of("cluster", "CLUSTER usuarios USING idx_usuarios_google_subject_id", true),
                Arguments.of("outra tabela", "DROP INDEX idx_usuarios_google_subject_id; "
                        + "CREATE INDEX idx_usuarios_google_subject_id ON contas(descricao)", true));
    }

    @Test
    void preservaIndiceComDependenciaDeExtensao() throws SQLException {
        flyway("1").migrate();
        executar("ALTER INDEX idx_usuarios_google_subject_id DEPENDS ON EXTENSION plpgsql");
        try {
            flyway(null).migrate();
            assertThat(indiceExiste("idx_usuarios_google_subject_id")).isTrue();
            verificarCatalogoTenant();
        } finally {
            executar("ALTER INDEX idx_usuarios_google_subject_id NO DEPENDS ON EXTENSION plpgsql");
        }
    }

    @Test
    void indiceObrigatorioPreexistenteDiferenteImpedeFalsoSucessoERollbackPreservaV1() throws SQLException {
        flyway("1").migrate();
        executar("CREATE INDEX idx_contas_usuario_id ON contas(descricao)");
        var erro = assertThrows(FlywayException.class, () -> flyway(null).migrate());
        assertThat(erro).hasStackTraceContaining("Índice obrigatório de tenant inválido ou diferente");
        assertThat(flyway("1").info().current().getVersion().getVersion()).isEqualTo("1");
        assertThat(indiceExiste("idx_categorias_usuario_id")).isFalse();
        assertThat(indiceExiste("idx_usuarios_google_subject_id")).isTrue();
    }

    static Stream<Arguments> indicesObrigatoriosInutilizaveis() {
        return Stream.of("contas", "categorias").flatMap(tabela -> Stream.of(
                Arguments.of(tabela, "invalido"), Arguments.of(tabela, "collation diferente")));
    }

    @ParameterizedTest(name = "Índice obrigatório inutilizável: {0}, {1}")
    @MethodSource("indicesObrigatoriosInutilizaveis")
    void indiceObrigatorioInutilizavelImpedeUpgradeComRollback(String tabela, String problema) throws SQLException {
        flyway("1").migrate();
        String indice = "idx_" + tabela + "_usuario_id";
        String collation = problema.equals("collation diferente") ? collationDiferente(tabela) : null;
        executar("CREATE INDEX " + indice + " ON " + tabela + "(usuario_id"
                + (collation == null ? "" : " COLLATE \"" + collation + "\"") + ")");
        if (collation == null) {
            executar("UPDATE pg_index SET indisvalid=false WHERE indexrelid='" + indice + "'::regclass");
        } else {
            verificarCollationDivergente(tabela, indice);
        }
        verificarFalhaERollbackV1("Índice obrigatório de tenant inválido ou diferente");
    }

    static Stream<Arguments> coberturasInutilizaveisDasMovimentacoes() {
        return MOVIMENTACOES.stream().flatMap(tabela -> Stream.of(
                Arguments.of(tabela, "ausente"), Arguments.of(tabela, "invalido"),
                Arguments.of(tabela, "collation diferente")));
    }

    @ParameterizedTest(name = "Composto de movimentação inutilizável: {0}, {1}")
    @MethodSource("coberturasInutilizaveisDasMovimentacoes")
    void coberturaInutilizavelDeMovimentacaoImpedeUpgradeComRollback(String tabela, String problema) throws SQLException {
        flyway("1").migrate();
        String indice = "idx_" + tabela + "_usuario_" + (tabela.endsWith("fixas") ? "data_inicio" : "datas");
        if (problema.equals("invalido")) {
            executar("UPDATE pg_index SET indisvalid=false WHERE indexrelid='" + indice + "'::regclass");
        } else {
            executar("DROP INDEX " + indice);
            if (problema.equals("collation diferente")) {
                executar("CREATE INDEX " + indice + " ON " + tabela + "(usuario_id COLLATE \""
                        + collationDiferente(tabela) + "\",data_inicio" + (tabela.endsWith("fixas") ? ")" : ",data_fim)"));
                verificarCollationDivergente(tabela, indice);
            }
        }
        verificarFalhaERollbackV1("Cobertura de tenant inutilizável no índice composto existente");
        assertThat(indiceExiste("idx_contas_usuario_id")).isFalse();
        assertThat(indiceExiste("idx_categorias_usuario_id")).isFalse();
    }

    private String collationDiferente(String tabela) throws SQLException {
        return texto("SELECT c.collname FROM pg_collation c JOIN pg_namespace n ON n.oid=c.collnamespace "
                + "WHERE n.nspname='pg_catalog' AND c.collname IN ('C','POSIX') "
                + "AND c.oid <> (SELECT attcollation FROM pg_attribute WHERE attrelid='" + tabela
                + "'::regclass AND attname='usuario_id') ORDER BY c.collname LIMIT 1");
    }

    private void verificarCollationDivergente(String tabela, String indice) throws SQLException {
        assertThat(numero("SELECT count(*) FROM pg_index i JOIN pg_attribute a "
                + "ON a.attrelid=i.indrelid AND a.attname='usuario_id' WHERE i.indexrelid='" + indice
                + "'::regclass AND a.attrelid='" + tabela + "'::regclass AND i.indcollation[0]<>a.attcollation"))
                .as("A collation escolhida deve divergir efetivamente da coluna no contêiner").isEqualTo(1);
    }

    private void verificarFalhaERollbackV1(String mensagem) throws SQLException {
        var checksumV1 = flyway("1").info().current().getChecksum();
        var indicesAntes = texto("SELECT string_agg(pg_get_indexdef(i.indexrelid) || ':' || i.indisvalid "
                + "|| ':' || i.indisready || ':' || i.indcollation::text, ';' ORDER BY c.relname) "
                + "FROM pg_index i JOIN pg_class c ON c.oid=i.indexrelid "
                + "WHERE c.relnamespace=current_schema()::regnamespace");
        var fksAntes = catalogoFks();
        var erro = assertThrows(FlywayException.class, () -> flyway(null).migrate());
        assertThat(erro).hasStackTraceContaining(mensagem);
        var aplicadas = flyway("1").info().applied();
        assertThat(aplicadas).hasSize(1);
        assertThat(aplicadas[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(aplicadas[0].getChecksum()).isEqualTo(checksumV1);
        assertThat(flyway("1").validateWithResult().validationSuccessful).isTrue();
        assertThat(texto("SELECT string_agg(pg_get_indexdef(i.indexrelid) || ':' || i.indisvalid "
                + "|| ':' || i.indisready || ':' || i.indcollation::text, ';' ORDER BY c.relname) "
                + "FROM pg_index i JOIN pg_class c ON c.oid=i.indexrelid "
                + "WHERE c.relnamespace=current_schema()::regnamespace")).isEqualTo(indicesAntes);
        verificarFksPreservadas(fksAntes);
        assertThat(indiceExiste("idx_usuarios_google_subject_id")).isTrue();
    }

    @ParameterizedTest(name = "Preserva catálogo: {0}")
    @MethodSource("catalogosNaoRedundantes")
    void preservaQuandoPrecondicaoDeRedundanciaNaoForComprovada(
            String cenario, String preparacao, boolean secundarioPresente) throws SQLException {
        flyway("1").migrate();
        executar(preparacao);
        var antes = texto("SELECT string_agg(indexdef, ';' ORDER BY indexname) "
                + "FROM pg_indexes WHERE schemaname=current_schema() AND tablename='usuarios'");
        flyway(null).migrate();
        assertThat(indiceExiste("idx_usuarios_google_subject_id")).as(cenario).isEqualTo(secundarioPresente);
        assertThat(texto("SELECT string_agg(indexdef, ';' ORDER BY indexname) "
                + "FROM pg_indexes WHERE schemaname=current_schema() AND tablename='usuarios'")).isEqualTo(antes);
        verificarCatalogoTenant();
        assertThat(flyway(null).validateWithResult().validationSuccessful).isTrue();
    }

    private void verificarCatalogoTenant() throws SQLException {
        var tabelas = new ArrayList<>(MOVIMENTACOES);
        tabelas.addAll(List.of("contas", "categorias"));
        for (var tabela : tabelas) {
            assertThat(numero("SELECT count(*) FROM pg_index i JOIN pg_attribute a "
                    + "ON a.attrelid=i.indrelid AND a.attnum=i.indkey[0] "
                    + "WHERE i.indrelid='" + tabela + "'::regclass AND i.indisvalid AND i.indisready "
                    + "AND i.indislive AND i.indpred IS NULL AND a.attname='usuario_id' "
                    + "AND i.indcollation[0]=a.attcollation")).as(tabela).isEqualTo(1);
        }
        assertThat(indiceExiste("idx_contas_usuario_id")).isTrue();
        assertThat(indiceExiste("idx_categorias_usuario_id")).isTrue();
        for (var tabela : MOVIMENTACOES) {
            String sufixo = tabela.endsWith("fixas") ? "data_inicio" : "datas";
            assertThat(indiceExiste("idx_" + tabela + "_usuario_" + sufixo)).isTrue();
            // Os índices temporais da V1 permanecem até haver justificativa para remoção.
            assertThat(indiceExiste("idx_" + tabela + "_" + sufixo)).isTrue();
            assertThat(numero("SELECT count(*) FROM pg_index i JOIN pg_attribute a "
                    + "ON a.attrelid=i.indrelid AND a.attnum=i.indkey[0] "
                    + "WHERE i.indrelid='" + tabela + "'::regclass AND i.indnkeyatts=1 "
                    + "AND a.attname='usuario_id'")).isZero();
        }
    }

    private void inserirDadosFinanceiros() throws SQLException {
        executar("INSERT INTO contas(id,usuario_id,descricao) VALUES (10,'tenant-a','Conta A'),(11,'tenant-b','Conta B')");
        executar("INSERT INTO categorias(id,usuario_id,descricao) VALUES (20,'tenant-a','Categoria A'),(21,'tenant-b','Categoria B')");
        for (var tabela : MOVIMENTACOES) {
            String valores = tabela.endsWith("fixas") ? "valor" : "valor_parcela,quantidade_parcelas,data_fim";
            String dados = tabela.endsWith("fixas") ? "123.45" : "123.45,3,DATE '2026-12-01'";
            executar("INSERT INTO " + tabela + "(usuario_id,descricao,conta_id,categoria_id,data_inicio," + valores
                    + ") VALUES ('tenant-a','Original',10,20,DATE '2026-10-01'," + dados + ")");
        }
    }

    private void verificarDadosFinanceiros() throws SQLException {
        assertThat(numero("SELECT count(*) FROM contas")).isEqualTo(2);
        assertThat(numero("SELECT count(*) FROM categorias")).isEqualTo(2);
        for (var tabela : MOVIMENTACOES) {
            var valor = tabela.endsWith("fixas") ? "valor" : "valor_parcela";
            assertThat(texto("SELECT usuario_id || ':' || descricao || ':' || " + valor
                    + " || ':' || conta_id || ':' || categoria_id || ':' || data_inicio FROM " + tabela))
                    .isEqualTo("tenant-a:Original:123.45:10:20:2026-10-01");
            if (tabela.endsWith("variaveis")) {
                assertThat(texto("SELECT quantidade_parcelas || ':' || data_fim FROM " + tabela))
                        .isEqualTo("3:2026-12-01");
            }
            assertThat(violacao("DELETE FROM contas WHERE id=10")).isEqualTo("23503");
            assertThat(violacao("DELETE FROM categorias WHERE id=20")).isEqualTo("23503");
            assertThat(violacao("UPDATE " + tabela + " SET conta_id=9999")).isEqualTo("23503");
            assertThat(violacao("UPDATE " + tabela + " SET categoria_id=9999")).isEqualTo("23503");
        }
    }

    private void verificarIntegridade() throws SQLException {
        executar("INSERT INTO usuarios(google_subject_id) VALUES ('teste-unique') ON CONFLICT DO NOTHING");
        assertThat(violacao("INSERT INTO usuarios(google_subject_id) VALUES ('teste-unique')")).isEqualTo("23505");
        assertThat(numero("SELECT count(*) FROM pg_index i JOIN pg_attribute a "
                + "ON a.attrelid=i.indrelid AND a.attnum=i.indkey[0] "
                + "WHERE i.indrelid='usuarios'::regclass AND i.indisunique AND i.indisvalid "
                + "AND i.indnkeyatts=1 AND i.indnatts=1 AND a.attname='google_subject_id'")).isEqualTo(1);
        assertThat(numero("SELECT count(*) FROM pg_constraint WHERE connamespace=current_schema()::regnamespace "
                + "AND contype='f' AND convalidated")).isEqualTo(8);
    }

    private void verificarFksPreservadas(String fksV1) throws SQLException {
        assertThat(numero("SELECT count(*) FROM pg_constraint WHERE connamespace=current_schema()::regnamespace "
                + "AND contype='f'")).isEqualTo(8);
        assertThat(catalogoFks()).isEqualTo(fksV1);
    }

    private String catalogoFks() throws SQLException {
        return texto("""
                SELECT jsonb_agg(jsonb_build_object(
                    'tabela', t.relname, 'nome', c.conname,
                    'colunas', (SELECT array_agg(a.attname ORDER BY k.ord)
                        FROM unnest(c.conkey) WITH ORDINALITY k(attnum,ord)
                        JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=k.attnum),
                    'tabela_referenciada', r.relname,
                    'colunas_referenciadas', (SELECT array_agg(a.attname ORDER BY k.ord)
                        FROM unnest(c.confkey) WITH ORDINALITY k(attnum,ord)
                        JOIN pg_attribute a ON a.attrelid=c.confrelid AND a.attnum=k.attnum),
                    'definicao', pg_get_constraintdef(c.oid, true), 'validada', c.convalidated,
                    'acao_update', c.confupdtype, 'acao_delete', c.confdeltype, 'match', c.confmatchtype,
                    'adiavel', c.condeferrable, 'inicialmente_adiada', c.condeferred
                ) ORDER BY t.relname,c.conname)::text
                FROM pg_constraint c JOIN pg_class t ON t.oid=c.conrelid
                JOIN pg_class r ON r.oid=c.confrelid
                WHERE c.contype='f' AND c.connamespace=current_schema()::regnamespace
                """);
    }

    private String violacao(String sql) {
        return assertThrows(SQLException.class, () -> executar(sql)).getSQLState();
    }

    private boolean indiceExiste(String nome) throws SQLException {
        return numero("SELECT count(*) FROM pg_class WHERE relnamespace=current_schema()::regnamespace "
                + "AND relkind='i' AND relname='" + nome + "'") == 1;
    }

    private Flyway flyway(String target) {
        var config = Flyway.configure().dataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas(schema).defaultSchema(schema).locations("classpath:db/migration");
        if (target != null) {
            config.target(target);
        }
        return config.load();
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private void executar(String sql) throws SQLException {
        try (var connection = conectar(); var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private String texto(String sql) throws SQLException {
        try (var connection = conectar(); var statement = connection.createStatement();
                var result = statement.executeQuery(sql)) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }

    private long numero(String sql) throws SQLException {
        return Long.parseLong(texto(sql));
    }
}

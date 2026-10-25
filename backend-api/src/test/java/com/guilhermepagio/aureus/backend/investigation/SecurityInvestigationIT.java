package com.guilhermepagio.aureus.backend.investigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.UsuarioRepository;
import com.guilhermepagio.aureus.backend.security.CurrentTenantIdentifierResolverImpl;
import com.guilhermepagio.aureus.backend.security.JwtUtil;
import com.guilhermepagio.aureus.backend.security.OAuth2LoginSuccessHandler;
import com.guilhermepagio.aureus.backend.security.TenantContext;

/** Caracteriza caminhos internos e HTTP sem modificar o contrato de produção. */
public class SecurityInvestigationIT extends InvestigationSupport {
    @Autowired private ContaRepository contas;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private OAuth2LoginSuccessHandler oauth;
    @Autowired private Flyway flyway;
    @Autowired private ApplicationContext context;

    SecurityInvestigationIT(MockMvc mockMvc, JwtUtil jwtUtil, @Value("${jwt.secret}") String jwtSecret) {
        super(mockMvc, jwtUtil, jwtSecret);
    }

    @Test
    void caracterizaSessaoFallbackBootstrapEOAuthSemPublicarCredenciais(InvestigationEvidence.Run run) throws Exception {
        var evidence = new LinkedHashMap<String, Object>();
        cleanDatabase();
        try {
            for (int i = 0; i < TENANTS.size(); i++) {
                jdbc.update("INSERT INTO contas(id,usuario_id,descricao) VALUES (?,?,?)", i + 11, TENANTS.get(i), "Conta " + i);
            }
            evidence.put("session", investigateReplay());
            evidence.put("bootstrap", investigateBootstrap());
            evidence.put("missingTenant", investigateMissingTenant());
            evidence.put("existingSession", investigateExistingSession());
            evidence.put("oauthWithoutTenant", investigateOAuth());
            evidence.put("httpBoundary", Map.of("anonymousStatus", statusOf(null), "authenticatedAStatus", statusOf("invest-a"),
                    "authenticatedBStatus", statusOf("invest-b"), "authenticatedPublicStatus", statusOf("public"),
                    "publicRowsAnonymousExposed", false));
            assertThat(TenantContext.getTenantId()).isNull();
        } finally {
            TenantContext.clear();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            cleanDatabase();
        }
        complete("security", evidence, run);
    }

    private Map<String, Object> investigateReplay() throws Exception {
        long issuedBefore = System.currentTimeMillis();
        var copied = authCookie("invest-a");
        assertThat(mockMvc.perform(get("/api/contas").cookie(copied)).andReturn().getResponse().getStatus()).isEqualTo(200);
        var withoutCsrf = mockMvc.perform(post("/api/auth/logout").cookie(copied)).andReturn().getResponse();
        assertThat(withoutCsrf.getStatus()).isEqualTo(403);
        var logout = mockMvc.perform(post("/api/auth/logout").cookie(copied).with(csrf())).andReturn().getResponse();
        assertThat(logout.getStatus()).isEqualTo(200);
        assertThat(logout.getHeaders("Set-Cookie")).anyMatch(value -> value.startsWith("AUREUS_SESSION=;") && value.contains("Max-Age=0"));
        int replay = mockMvc.perform(get("/api/contas").cookie(copied)).andReturn().getResponse().getStatus();
        assertThat(replay).isEqualTo(200);
        var expired = mockMvc.perform(get("/api/contas").cookie(expiredAuthCookie("invest-a"))).andReturn().getResponse();
        assertThat(expired.getStatus()).isEqualTo(401);
        assertThat(expired.getHeaders("Set-Cookie")).anyMatch(value -> value.contains("Max-Age=0"));
        // Falha anterior à entrega: não houve POST nem resposta capaz de apagar o cookie.
        // É um modelo explícito de requisição não entregue, não uma medição de rede.
        var notDelivered = authCookie("invest-b");
        assertThat(mockMvc.perform(get("/api/contas").cookie(notDelivered)).andReturn().getResponse().getStatus()).isEqualTo(200);
        return Map.of("logoutWithCsrfStatus", 200, "logoutWithoutCsrfStatus", 403, "copiedJwtReplayAfterLogoutStatus", replay,
                "expiredJwtStatus", 401, "configuredTtlMs", jwtUtil.getExpiration(),
                "residualWindowLowerBoundMs", jwtUtil.getExpiration() - (System.currentTimeMillis() - issuedBefore) - 1000,
                "residualWindowUpperBoundMs", jwtUtil.getExpiration(),
                "undeliveredLogoutModelReplayStatus", 200,
                "clientCleanupEvidence", "frontend-web/src/components/Header/Header.test.tsx; execução separada; MockMvc não executa navegador");
    }

    private Map<String, Object> investigateBootstrap() {
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().applied()).hasSize(2);
        var effective = factory().getCurrentTenantIdentifierResolver();
        assertThat(effective).isInstanceOf(CurrentTenantIdentifierResolverImpl.class);
        assertThat(effective.validateExistingCurrentSessions()).isTrue();
        var springBean = context.getBean(CurrentTenantIdentifierResolverImpl.class);
        return Map.of("flywayAppliedVersions", List.of("1", "2"), "flywayValidationSuccessful", true,
                "jpaDdlAuto", emf.getProperties().get("hibernate.hbm2ddl.auto"),
                "effectiveResolverClass", effective.getClass().getName(), "sameInstanceAsSpringBean", (Object) effective == springBean,
                "validateExistingCurrentSessions", true, "startupWithoutTenantSuccessful", true);
    }

    private List<Map<String, Object>> investigateMissingTenant() {
        var observations = new ArrayList<Map<String, Object>>();
        for (String missing : new String[] { null, "", "   " }) {
            var row = new LinkedHashMap<String, Object>();
            row.put("context", missing == null ? "null" : missing.isEmpty() ? "empty" : "blank");
            tenant(missing, () -> {
                SqlTrace.begin();
                var em = emf.createEntityManager();
                try {
                    em.getTransaction().begin();
                    var session = em.unwrap(org.hibernate.Session.class);
                    assertThat(session.getTenantIdentifierValue()).isEqualTo("public");
                    var list = em.createQuery("from Conta c order by c.id", Conta.class).getResultList();
                    assertThat(list).extracting(Conta::getUsuarioId).containsOnly("public");
                    row.put("newSessionTenant", session.getTenantIdentifierValue());
                    row.put("queryOwners", list.stream().map(Conta::getUsuarioId).toList());
                    assertThat(em.createQuery("from Conta c where c.usuarioId = :owner", Conta.class)
                            .setParameter("owner", "invest-a").getResultList()).isEmpty();
                    var byId = em.find(Conta.class, 11L);
                    assertThat(byId).as("Contexto ausente não pode ler ID de outro tenant").isNull();
                    row.put("foreignIdLookupOwner", byId == null ? "not-found" : byId.getUsuarioId());
                    var created = new Conta(null, "Fallback", "Sintética");
                    em.persist(created);
                    em.flush();
                    assertThat(created.getUsuarioId()).isEqualTo("public");
                    row.put("persistedOwner", created.getUsuarioId());
                    em.getTransaction().rollback();
                } finally {
                    if (em.getTransaction().isActive()) em.getTransaction().rollback();
                    em.close();
                    row.put("normalizedSql", SqlTrace.end());
                }
                // Consulta nativa deliberadamente global, usada para rejeitar vínculos alheios.
                SqlTrace.begin();
                try {
                    assertThat(contas.findOwnerUsuarioId(11L)).contains("invest-a");
                    row.put("nativeOwnerLookup", "invest-a");
                } finally {
                    row.put("nativeOwnerNormalizedSql", SqlTrace.end());
                }
                return null;
            });
            System.out.println("Investigação 7.5: contexto=" + row.get("context") + " sessãoNova=" + row.get("newSessionTenant")
                    + " buscaIdAlheio=" + row.get("foreignIdLookupOwner") + " escrita=" + row.get("persistedOwner"));
            observations.add(row);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM contas", Long.class)).isEqualTo(3);
        return observations;
    }

    private List<Map<String, Object>> investigateExistingSession() {
        var result = new ArrayList<Map<String, Object>>();
        for (String missing : new String[] { null, "", "   " }) {
            result.add(tenant("invest-a", () -> {
            var em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                em.createQuery("from Conta", Conta.class).getResultList();
                TenantContext.setTenantId(missing);
                SqlTrace.begin();
                var owners = em.createQuery("from Conta", Conta.class).getResultList().stream().map(Conta::getUsuarioId).toList();
                assertThat(owners).containsExactly("invest-a");
                var created = new Conta(null, "Sessao existente", null);
                em.persist(created);
                em.flush();
                assertThat(created.getUsuarioId()).isEqualTo("invest-a");
                var fresh = emf.createEntityManager();
                try {
                    assertThat(fresh.unwrap(org.hibernate.Session.class).getTenantIdentifierValue()).isEqualTo("public");
                } finally {
                    fresh.close();
                }
                em.getTransaction().rollback();
                return Map.of("sessionOpenedFor", "invest-a", "contextAfterOpen", missing == null ? "null" : missing.isEmpty() ? "empty" : "blank",
                        "existingSessionQueryOwners", owners, "normalizedSql", SqlTrace.end(),
                        "existingSessionPersistedOwner", created.getUsuarioId(), "newSessionTenant", "public",
                        "limit", "EntityManager explícito mantém tenant da abertura; validateExistingCurrentSessions não revalida cada consulta");
            } finally {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                em.close();
                SqlTrace.end();
            }
            }));
        }
        return result;
    }

    private Map<String, Object> investigateOAuth() throws Exception {
        TenantContext.clear();
        var financialBefore = financialSnapshot();
        var principal = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "investigation75-subject", "name", "Sintético", "email", "synthetic@example.invalid"), "sub");
        var auth = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");
        var request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        var response = new MockHttpServletResponse();
        oauth.onAuthenticationSuccess(request, response, auth);
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(usuarios.findByGoogleSubjectId("investigation75-subject")).isPresent();
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(financialSnapshot()).as("OAuth global não altera nenhuma das seis tabelas financeiras").isEqualTo(financialBefore);
        return Map.of("tenantContext", "null", "globalUserPersisted", true, "financialRowsCreated", 0,
                "financialTablesCompared", TABLES,
                "redirectStatus", 302, "limit", "Principal sintético; não chama Google nem valida a troca OAuth externa");
    }

    private int statusOf(String owner) throws Exception {
        var request = get("/api/contas");
        if (owner != null) request.cookie(authCookie(owner));
        var response = mockMvc.perform(request).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(owner == null ? 401 : 200);
        if (owner != null) {
            var rows = objectMapper.readTree(response.getContentAsByteArray());
            assertThat(rows).hasSize(1);
            assertThat(rows.get(0).get("id").asLong()).isEqualTo(TENANTS.indexOf(owner) + 11L);
        }
        return response.getStatus();
    }
}

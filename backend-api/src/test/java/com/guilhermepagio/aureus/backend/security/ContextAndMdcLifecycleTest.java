package com.guilhermepagio.aureus.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.guilhermepagio.aureus.backend.controller.AuthController;
import com.guilhermepagio.aureus.backend.domain.Categoria;
import com.guilhermepagio.aureus.backend.domain.Conta;
import com.guilhermepagio.aureus.backend.domain.DespesaFixa;
import com.guilhermepagio.aureus.backend.domain.DespesaVariavel;
import com.guilhermepagio.aureus.backend.domain.ReceitaFixa;
import com.guilhermepagio.aureus.backend.domain.ReceitaVariavel;
import com.guilhermepagio.aureus.backend.domain.Usuario;
import com.guilhermepagio.aureus.backend.domain.dto.CategoriaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.DespesaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.IdReferenceDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaFixaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ReceitaVariavelRequestDTO;
import com.guilhermepagio.aureus.backend.exception.GlobalExceptionHandler;
import com.guilhermepagio.aureus.backend.exception.ResourceNotFoundException;
import com.guilhermepagio.aureus.backend.repository.CategoriaRepository;
import com.guilhermepagio.aureus.backend.repository.ContaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.DespesaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaFixaRepository;
import com.guilhermepagio.aureus.backend.repository.ReceitaVariavelRepository;
import com.guilhermepagio.aureus.backend.repository.UsuarioRepository;
import com.guilhermepagio.aureus.backend.service.CategoriaService;
import com.guilhermepagio.aureus.backend.service.ConsolidacaoService;
import com.guilhermepagio.aureus.backend.service.ContaService;
import com.guilhermepagio.aureus.backend.service.DespesaFixaService;
import com.guilhermepagio.aureus.backend.service.DespesaVariavelService;
import com.guilhermepagio.aureus.backend.service.ReceitaFixaService;
import com.guilhermepagio.aureus.backend.service.ReceitaVariavelService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

@ExtendWith(OutputCaptureExtension.class)
class ContextAndMdcLifecycleTest {

    private static final String JWT_SECRET = "8f8a8473822a4d70b33b006a6b82b9937b8b4abcf35e408dbb0a1d4a04791e84";
    private static final long JWT_EXPIRATION = 86_400_000L;

    private JwtUtil jwtUtil;
    private JwtAuthenticationFilter filter;
    private Logger backendLogger;
    private Level previousLoggerLevel;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
        MDC.clear();

        jwtUtil = new JwtUtil(JWT_SECRET, JWT_EXPIRATION);
        filter = new JwtAuthenticationFilter(jwtUtil);

        backendLogger = (Logger) LoggerFactory.getLogger("com.guilhermepagio.aureus.backend");
        previousLoggerLevel = backendLogger.getLevel();
        backendLogger.setLevel(Level.DEBUG);

        listAppender = new ListAppender<>();
        listAppender.start();
        backendLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        if (backendLogger != null && listAppender != null) {
            backendLogger.detachAppender(listAppender);
            listAppender.stop();
            backendLogger.setLevel(previousLoggerLevel);
        }
        TenantContext.clear();
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    @DisplayName("Deve popular TenantContext, SecurityContextHolder e MDC durante requisição autenticada válida e limpar tudo em finally")
    void devePopularELimparContextosEMdcEmRequisicaoAutenticadaValida() throws Exception {
        final String token = jwtUtil.generateToken("user-1");
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/contas");
        request.setCookies(new Cookie("AUREUS_SESSION", token));
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final AtomicReference<String> capturedTenant = new AtomicReference<>();
        final AtomicReference<Object> capturedPrincipal = new AtomicReference<>();
        final AtomicReference<String> capturedMdcRequestId = new AtomicReference<>();
        final AtomicReference<String> capturedMdcTenantId = new AtomicReference<>();

        final FilterChain chain = (req, res) -> {
            capturedTenant.set(TenantContext.getTenantId());
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            capturedPrincipal.set(auth != null ? auth.getPrincipal() : null);
            capturedMdcRequestId.set(MDC.get("requestId"));
            capturedMdcTenantId.set(MDC.get("tenantId"));
        };

        filter.doFilter(request, response, chain);

        assertThat(capturedTenant.get()).isEqualTo("user-1");
        assertThat(capturedPrincipal.get()).isEqualTo("user-1");
        assertThat(capturedMdcRequestId.get()).isNotBlank().hasSize(8);
        assertThat(capturedMdcTenantId.get()).isEqualTo("user-1");

        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("tenantId")).isNull();
    }

    @Test
    @DisplayName("Deve rejeitar token inválido e expirado, manter contexto nulo e MDC anon, limpar cookie e não vazar token nos logs")
    void deveManterContextoLimpoEMdcAnonEmTokenInvalidoEExpiradoSemVazarTokenNosLogs(final CapturedOutput output) throws Exception {
        final String invalidTokenValue = "eyJhbGciOiJIUzI1NiJ9.token-invalido-super-secreto-xyz.assinatura";
        final MockHttpServletRequest invalidRequest = new MockHttpServletRequest("GET", "/api/contas");
        invalidRequest.setCookies(new Cookie("AUREUS_SESSION", invalidTokenValue));
        final MockHttpServletResponse invalidResponse = new MockHttpServletResponse();

        final AtomicReference<String> tenantDuringInvalid = new AtomicReference<>("dirty");
        final AtomicReference<Authentication> authDuringInvalid = new AtomicReference<>();
        final AtomicReference<String> mdcTenantDuringInvalid = new AtomicReference<>();
        final AtomicReference<String> mdcReqDuringInvalid = new AtomicReference<>();

        filter.doFilter(invalidRequest, invalidResponse, (req, res) -> {
            tenantDuringInvalid.set(TenantContext.getTenantId());
            authDuringInvalid.set(SecurityContextHolder.getContext().getAuthentication());
            mdcTenantDuringInvalid.set(MDC.get("tenantId"));
            mdcReqDuringInvalid.set(MDC.get("requestId"));
        });

        assertThat(tenantDuringInvalid.get()).isNull();
        assertThat(authDuringInvalid.get()).isNull();
        assertThat(mdcTenantDuringInvalid.get()).isEqualTo("anon");
        assertThat(mdcReqDuringInvalid.get()).isNotBlank();
        assertThat(invalidResponse.getHeader(HttpHeaders.SET_COOKIE))
                .contains("AUREUS_SESSION=;")
                .contains("Max-Age=0");

        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("tenantId")).isNull();

        final String expiredTokenValue = gerarTokenExpirado("user-expirado-99");
        final MockHttpServletRequest expiredRequest = new MockHttpServletRequest("POST", "/api/despesas-fixas");
        expiredRequest.setCookies(new Cookie("AUREUS_SESSION", expiredTokenValue));
        final MockHttpServletResponse expiredResponse = new MockHttpServletResponse();

        filter.doFilter(expiredRequest, expiredResponse, (req, res) -> {
            assertThat(TenantContext.getTenantId()).isNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            assertThat(MDC.get("tenantId")).isEqualTo("anon");
            assertThat(MDC.get("requestId")).isNotBlank();
        });

        assertThat(expiredResponse.getHeader(HttpHeaders.SET_COOKIE))
                .contains("AUREUS_SESSION=;")
                .contains("Max-Age=0");
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("tenantId")).isNull();

        final List<String> formattedLogs = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        assertThat(formattedLogs)
                .anyMatch(msg -> msg.contains("method=GET") && msg.contains("uri=/api/contas") && msg.contains("motivo=token_invalido"))
                .anyMatch(msg -> msg.contains("method=POST") && msg.contains("uri=/api/despesas-fixas") && msg.contains("motivo=token_expirado"));

        for (final String msg : formattedLogs) {
            assertThat(msg)
                    .doesNotContain(invalidTokenValue)
                    .doesNotContain("token-invalido-super-secreto-xyz")
                    .doesNotContain(expiredTokenValue);
        }
        assertThat(output.getAll())
                .doesNotContain(invalidTokenValue)
                .doesNotContain("token-invalido-super-secreto-xyz")
                .doesNotContain(expiredTokenValue);
    }

    @Test
    @DisplayName("Deve rejeitar token com subject vazio ou em branco, manter contexto nulo e MDC anon")
    void deveRejeitarTokenComSubjectVazioOuEmBrancoELimparContexto() throws Exception {
        final SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        final String emptySubjectToken = Jwts.builder()
                .subject("")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(key)
                .compact();

        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/contas");
        request.setCookies(new Cookie("AUREUS_SESSION", emptySubjectToken));
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertThat(TenantContext.getTenantId()).isNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            assertThat(MDC.get("tenantId")).isEqualTo("anon");
            assertThat(MDC.get("requestId")).isNotBlank();
        });

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("AUREUS_SESSION=;")
                .contains("Max-Age=0");
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("tenantId")).isNull();

        final List<String> formattedLogs = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        assertThat(formattedLogs)
                .anyMatch(msg -> msg.contains("method=GET") && msg.contains("uri=/api/contas")
                        && msg.contains("motivo=token_invalido") && msg.contains("excecao=IllegalArgumentException"));
    }

    @Test
    @DisplayName("Deve limpar TenantContext, SecurityContextHolder e MDC mesmo quando FilterChain lançar exceção")
    void deveLimparContextosEMdcMesmoQuandoFilterChainLancarExcecao() {
        final String token = jwtUtil.generateToken("user-excecao");
        final MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/contas/10");
        request.setCookies(new Cookie("AUREUS_SESSION", token));
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final FilterChain failingChain = (req, res) -> {
            assertThat(TenantContext.getTenantId()).isEqualTo("user-excecao");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(MDC.get("tenantId")).isEqualTo("user-excecao");
            assertThat(MDC.get("requestId")).isNotBlank();
            throw new ServletException("Falha simulada durante processamento da cadeia");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, failingChain))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("Falha simulada");

        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("tenantId")).isNull();
    }

    @Test
    @DisplayName("Deve garantir isolamento no reuso da mesma thread entre user-1, user-2 e chamada anônima")
    void deveIsolarContextosEMdcAoReutilizarMesmaThreadEntreUsuariosEChamadaAnonima() throws Exception {
        final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        try {
            final AtomicLong threadIdUser1 = new AtomicLong();
            final AtomicLong threadIdUser2 = new AtomicLong();
            final AtomicLong threadIdAnon = new AtomicLong();

            final AtomicReference<String> reqId1 = new AtomicReference<>();
            final AtomicReference<String> reqId2 = new AtomicReference<>();
            final AtomicReference<String> reqId3 = new AtomicReference<>();

            // 1. Execução na thread para user-1 (com exceção para provar limpeza sob falha antes do reuso)
            singleThreadExecutor.submit(() -> {
                threadIdUser1.set(Thread.currentThread().threadId());
                final MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/contas");
                req1.setCookies(new Cookie("AUREUS_SESSION", jwtUtil.generateToken("user-1")));
                final MockHttpServletResponse res1 = new MockHttpServletResponse();

                assertThatThrownBy(() -> filter.doFilter(req1, res1, (req, res) -> {
                    assertThat(TenantContext.getTenantId()).isEqualTo("user-1");
                    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("user-1");
                    assertThat(MDC.get("tenantId")).isEqualTo("user-1");
                    reqId1.set(MDC.get("requestId"));
                    throw new IllegalStateException("Erro forçado em user-1");
                })).isInstanceOf(IllegalStateException.class);

                assertThat(TenantContext.getTenantId()).isNull();
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
                assertThat(MDC.get("requestId")).isNull();
                assertThat(MDC.get("tenantId")).isNull();
                return null;
            }).get();

            // 2. Reuso da mesma thread para user-2
            singleThreadExecutor.submit(() -> {
                threadIdUser2.set(Thread.currentThread().threadId());
                final MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/contas");
                req2.setCookies(new Cookie("AUREUS_SESSION", jwtUtil.generateToken("user-2")));
                final MockHttpServletResponse res2 = new MockHttpServletResponse();

                filter.doFilter(req2, res2, (req, res) -> {
                    assertThat(TenantContext.getTenantId()).isEqualTo("user-2");
                    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("user-2");
                    assertThat(MDC.get("tenantId")).isEqualTo("user-2");
                    reqId2.set(MDC.get("requestId"));
                });

                assertThat(TenantContext.getTenantId()).isNull();
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
                assertThat(MDC.get("requestId")).isNull();
                assertThat(MDC.get("tenantId")).isNull();
                return null;
            }).get();

            // 3. Reuso da mesma thread para chamada anônima
            singleThreadExecutor.submit(() -> {
                threadIdAnon.set(Thread.currentThread().threadId());
                final MockHttpServletRequest req3 = new MockHttpServletRequest("GET", "/api/auth/me");
                final MockHttpServletResponse res3 = new MockHttpServletResponse();

                filter.doFilter(req3, res3, (req, res) -> {
                    assertThat(TenantContext.getTenantId()).isNull();
                    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
                    assertThat(MDC.get("tenantId")).isEqualTo("anon");
                    reqId3.set(MDC.get("requestId"));
                });

                assertThat(TenantContext.getTenantId()).isNull();
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
                assertThat(MDC.get("requestId")).isNull();
                assertThat(MDC.get("tenantId")).isNull();
                return null;
            }).get();

            assertThat(threadIdUser1.get())
                    .isEqualTo(threadIdUser2.get())
                    .isEqualTo(threadIdAnon.get());
            assertThat(reqId1.get()).isNotBlank();
            assertThat(reqId2.get()).isNotBlank().isNotEqualTo(reqId1.get());
            assertThat(reqId3.get()).isNotBlank().isNotEqualTo(reqId1.get()).isNotEqualTo(reqId2.get());

            assertThat(listAppender.list)
                    .anyMatch(e -> e.getLevel() == Level.DEBUG
                            && e.getFormattedMessage().contains("resultado=anonimo")
                            && e.getFormattedMessage().contains("motivo=cookie_ausente"));
        } finally {
            singleThreadExecutor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Deve configurar logging.pattern.level no application.yaml exibindo requestId e tenantId do MDC")
    void deveConfigurarPadraoDeLogNoApplicationYamlEFormatarMdcCorretamente() throws Exception {
        final YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yaml"));
        final Properties props = yamlFactory.getObject();

        assertThat(props).isNotNull();
        final String levelPattern = props.getProperty("logging.pattern.level");
        assertThat(levelPattern)
                .isNotNull()
                .contains("%X{requestId:-}")
                .contains("%X{tenantId:-}");

        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/contas");
        request.setCookies(new Cookie("AUREUS_SESSION", jwtUtil.generateToken("user-pattern-7")));
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final AtomicReference<String> requestIdInChain = new AtomicReference<>();
        filter.doFilter(request, response, (req, res) -> {
            requestIdInChain.set(MDC.get("requestId"));
            LoggerFactory.getLogger("com.guilhermepagio.aureus.backend.TestLogger").info("Evento correlacionado");
        });

        final ILoggingEvent correlatedEvent = listAppender.list.stream()
                .filter(e -> e.getFormattedMessage().contains("Evento correlacionado"))
                .findFirst()
                .orElseThrow();

        final PatternLayout layout = new PatternLayout();
        layout.setContext(backendLogger.getLoggerContext());
        layout.setPattern(levelPattern + " - %m%n");
        layout.start();

        final String rendered = layout.doLayout(correlatedEvent);
        assertThat(rendered)
                .contains("[req:" + requestIdInChain.get() + " tenant:user-pattern-7]")
                .contains("Evento correlacionado");
    }

    @Test
    @DisplayName("Deve registrar logs de login OAuth e logout com metadados técnicos sem expor dados pessoais, JWT ou cookies")
    void deveRegistrarLogsDeAutenticacaoSemExporDadosPessoaisJwtOuCookies(final CapturedOutput output) throws Exception {
        final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        final String secretSub = "google-sub-secreto-999888";
        final String secretEmail = "privado.secreto@aureus.test";
        final String secretName = "Nome Pessoal Secreto";
        final String secretPicture = "https://cdn.secreto/avatar-privado.png";

        when(usuarioRepository.findByGoogleSubjectId(secretSub)).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            final Usuario u = inv.getArgument(0);
            u.setId(77L);
            return u;
        });

        final OAuth2LoginSuccessHandler successHandler = new OAuth2LoginSuccessHandler(
                usuarioRepository, jwtUtil, "http://localhost:5173");

        final OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "sub", secretSub,
                        "email", secretEmail,
                        "name", secretName,
                        "picture", secretPicture
                ),
                "sub"
        );
        final OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                oAuth2User, oAuth2User.getAuthorities(), "google");

        final MockHttpServletRequest loginReq = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        final MockHttpServletResponse loginRes = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(loginReq, loginRes, authToken);

        final String setCookieHeader = loginRes.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isNotBlank();
        final String generatedJwt = extrairValorCookieSessao(setCookieHeader);
        assertThat(generatedJwt).isNotBlank();

        final AuthController authController = new AuthController(usuarioRepository);
        authController.me(null);

        final MockHttpServletRequest logoutReq = new MockHttpServletRequest("POST", "/api/auth/logout");
        logoutReq.setCookies(
                new Cookie("AUREUS_SESSION", generatedJwt),
                new Cookie("XSRF-TOKEN", "xsrf-secret-token-value-999"));
        authController.logout(logoutReq);

        final List<ILoggingEvent> events = listAppender.list;
        assertThat(events)
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("uri=/login/oauth2/code/google")
                        && e.getFormattedMessage().contains("resultado=sucesso")
                        && e.getFormattedMessage().contains("usuarioId=77"))
                .anyMatch(e -> e.getLevel() == Level.DEBUG
                        && e.getFormattedMessage().contains("uri=/api/auth/me")
                        && e.getFormattedMessage().contains("resultado=nao_autenticado"))
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("method=POST")
                        && e.getFormattedMessage().contains("uri=/api/auth/logout")
                        && e.getFormattedMessage().contains("resultado=sucesso"));

        for (final ILoggingEvent event : events) {
            assertThat(event.getFormattedMessage())
                    .doesNotContain(secretSub)
                    .doesNotContain(secretEmail)
                    .doesNotContain(secretName)
                    .doesNotContain(secretPicture)
                    .doesNotContain(generatedJwt)
                    .doesNotContain("xsrf-secret-token-value-999");
        }
        assertThat(output.getAll())
                .doesNotContain(secretSub)
                .doesNotContain(secretEmail)
                .doesNotContain(secretName)
                .doesNotContain(secretPicture)
                .doesNotContain(generatedJwt)
                .doesNotContain("xsrf-secret-token-value-999");
    }

    @Test
    @DisplayName("Deve registrar logs padronizados no GlobalExceptionHandler (WARN 400/403, DEBUG 404, ERROR 500) sem vazar dados sensíveis")
    void deveRegistrarLogsPadronizadosNoGlobalExceptionHandlerSemExporPayloadsOuValoresSensiveis(final CapturedOutput output)
            throws Exception {
        final GlobalExceptionHandler handler = new GlobalExceptionHandler();
        final MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/despesas-fixas");

        // 400 MethodArgumentNotValidException com valores rejeitados sensíveis
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "despesaFixa");
        bindingResult.addError(new FieldError(
                "despesaFixa", "descricao", "Descricao Secreta Rejeitada", false, null, null, "Descrição inválida"));
        bindingResult.addError(new FieldError(
                "despesaFixa", "valor", new BigDecimal("99887.66"), false, null, null, "Valor inválido"));
        final MethodParameter methodParam = new MethodParameter(
                ContextAndMdcLifecycleTest.class.getDeclaredMethod("setUp"), -1);
        handler.handleMethodArgumentNotValid(new MethodArgumentNotValidException(methodParam, bindingResult), req);

        // 400 DataIntegrityViolationException com detalhe SQL sensível
        handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("Detail: Key (descricao)=(Conta Privada SQL) already exists"), req);

        // 400 HttpMessageNotReadableException com payload bruto
        handler.handleHttpMessageNotReadable(
                new HttpMessageNotReadableException("Cannot deserialize valorParcela 44556.77", (HttpInputMessage) null), req);

        // 400 IllegalArgumentException, DateTimeParseException, TypeMismatch, MissingParam, ConstraintViolation, HandlerMethodValidation
        handler.handleIllegalArgument(new IllegalArgumentException("Argumento inválido"), req);
        handler.handleIllegalArgument(new DateTimeParseException("Text '2025-99-99' could not be parsed", "2025-99-99", 5), req);
        handler.handleTypeMismatchAndMissingParam(
                new MethodArgumentTypeMismatchException("valor-secreto-param", Long.class, "id", null, null), req);
        handler.handleTypeMismatchAndMissingParam(
                new MissingServletRequestParameterException("mesAno", "String"), req);

        final ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        final Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("mesAno");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("Formato inválido");
        handler.handleConstraintViolation(new ConstraintViolationException("Violação", Set.of(violation)), req);

        final HandlerMethodValidationException hmvEx = mock(HandlerMethodValidationException.class);
        when(hmvEx.getParameterValidationResults()).thenReturn(List.of());
        handler.handleHandlerMethodValidation(hmvEx, req);

        // 403 AccessDeniedException
        handler.handleAccessDenied(new AccessDeniedException("Acesso negado ao vínculo"), req);

        // 404 ResourceNotFoundException & NoResourceFoundException
        handler.handleResourceNotFound(new ResourceNotFoundException("Conta não encontrada: 55"), req);
        handler.handleNoResourceFound(new NoResourceFoundException(HttpMethod.GET, "/api/inexistente", "No resource"), req);

        // 405, 415, 406
        handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("DELETE", List.of("GET")), req);
        handler.handleMediaTypeNotSupported(new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)), req);
        handler.handleMediaTypeNotAcceptable(new HttpMediaTypeNotAcceptableException("Not acceptable"), req);

        // 500 Exception genérica com stack trace
        handler.handleGenericException(new RuntimeException("Falha interna simulada"), req);

        final List<ILoggingEvent> events = listAppender.list;

        assertThat(events)
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("method=POST")
                        && e.getFormattedMessage().contains("uri=/api/despesas-fixas")
                        && e.getFormattedMessage().contains("excecao=MethodArgumentNotValidException")
                        && e.getFormattedMessage().contains("regra=validacao_campos"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("excecao=DataIntegrityViolationException")
                        && e.getFormattedMessage().contains("regra=integridade_relacional"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("excecao=HttpMessageNotReadableException")
                        && e.getFormattedMessage().contains("regra=corpo_requisicao_invalido"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("excecao=IllegalArgumentException")
                        && e.getFormattedMessage().contains("regra=argumento_invalido"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("excecao=DateTimeParseException")
                        && e.getFormattedMessage().contains("regra=argumento_invalido"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("excecao=MethodArgumentTypeMismatchException")
                        && e.getFormattedMessage().contains("regra=parametro_invalido_ou_ausente"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("excecao=MissingServletRequestParameterException")
                        && e.getFormattedMessage().contains("regra=parametro_invalido_ou_ausente"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("excecao=ConstraintViolationException")
                        && e.getFormattedMessage().contains("regra=violacao_restricao_parametros"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=400")
                        && e.getFormattedMessage().contains("regra=validacao_metodo"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=403")
                        && e.getFormattedMessage().contains("excecao=AccessDeniedException")
                        && e.getFormattedMessage().contains("regra=acesso_negado"))
                .anyMatch(e -> e.getLevel() == Level.DEBUG
                        && e.getFormattedMessage().contains("status=404")
                        && e.getFormattedMessage().contains("excecao=ResourceNotFoundException")
                        && e.getFormattedMessage().contains("regra=recurso_nao_encontrado"))
                .anyMatch(e -> e.getLevel() == Level.DEBUG
                        && e.getFormattedMessage().contains("status=404")
                        && e.getFormattedMessage().contains("excecao=NoResourceFoundException")
                        && e.getFormattedMessage().contains("regra=rota_nao_encontrada"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=405")
                        && e.getFormattedMessage().contains("excecao=HttpRequestMethodNotSupportedException")
                        && e.getFormattedMessage().contains("regra=metodo_nao_suportado"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=415")
                        && e.getFormattedMessage().contains("excecao=HttpMediaTypeNotSupportedException")
                        && e.getFormattedMessage().contains("regra=midia_nao_suportada"))
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("status=406")
                        && e.getFormattedMessage().contains("excecao=HttpMediaTypeNotAcceptableException")
                        && e.getFormattedMessage().contains("regra=midia_nao_aceitavel"))
                .anyMatch(e -> e.getLevel() == Level.ERROR
                        && e.getFormattedMessage().contains("status=500")
                        && e.getFormattedMessage().contains("method=POST")
                        && e.getFormattedMessage().contains("uri=/api/despesas-fixas")
                        && e.getFormattedMessage().contains("excecao=RuntimeException")
                        && e.getFormattedMessage().contains("regra=erro_interno_inesperado")
                        && e.getThrowableProxy() != null);

        for (final ILoggingEvent event : events) {
            assertThat(event.getFormattedMessage())
                    .doesNotContain("Descricao Secreta Rejeitada")
                    .doesNotContain("99887.66")
                    .doesNotContain("Conta Privada SQL")
                    .doesNotContain("44556.77")
                    .doesNotContain("valor-secreto-param");
        }
        assertThat(output.getAll())
                .doesNotContain("Descricao Secreta Rejeitada")
                .doesNotContain("99887.66")
                .doesNotContain("Conta Privada SQL")
                .doesNotContain("44556.77")
                .doesNotContain("valor-secreto-param");
    }

    @Test
    @DisplayName("Deve registrar logs estruturados nos serviços financeiros e consolidação sem expor descrições ou valores monetários")
    void deveRegistrarLogsNosServicosFinanceirosEConsolidacaoSemExporValoresMonetariosOuDescricoes(final CapturedOutput output) {
        TenantContext.setTenantId("user-fin-1");

        final ContaRepository contaRepository = mock(ContaRepository.class);
        final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
        final DespesaFixaRepository despesaFixaRepository = mock(DespesaFixaRepository.class);
        final ReceitaFixaRepository receitaFixaRepository = mock(ReceitaFixaRepository.class);
        final DespesaVariavelRepository despesaVariavelRepository = mock(DespesaVariavelRepository.class);
        final ReceitaVariavelRepository receitaVariavelRepository = mock(ReceitaVariavelRepository.class);

        final Conta conta = new Conta(10L, "Conta Secreta VIP", "Obs Conta Confidencial");
        final Categoria categoria = new Categoria(20L, "Categoria Secreta VIP", "Obs Categoria Confidencial");

        when(contaRepository.save(any(Conta.class))).thenReturn(conta);
        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(contaRepository.existsById(10L)).thenReturn(true);
        when(contaRepository.findOwnerUsuarioId(10L)).thenReturn(Optional.of("user-fin-1"));
        when(contaRepository.findOwnerUsuarioId(99L)).thenReturn(Optional.of("outro-tenant"));

        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaRepository.findById(20L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsById(20L)).thenReturn(true);
        when(categoriaRepository.findOwnerUsuarioId(20L)).thenReturn(Optional.of("user-fin-1"));
        when(categoriaRepository.findOwnerUsuarioId(98L)).thenReturn(Optional.of("outro-tenant"));

        // ContaService & CategoriaService
        final ContaService contaService = new ContaService(contaRepository);
        contaService.criar(new ContaRequestDTO("Conta Secreta VIP", "Obs Conta Confidencial"));
        contaService.atualizar(10L, new ContaRequestDTO("Conta Secreta VIP", "Obs Conta Confidencial"));
        contaService.excluir(10L);

        final CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        categoriaService.criar(new CategoriaRequestDTO("Categoria Secreta VIP", "Obs Categoria Confidencial"));
        categoriaService.atualizar(20L, new CategoriaRequestDTO("Categoria Secreta VIP", "Obs Categoria Confidencial"));
        categoriaService.excluir(20L);

        // DespesaFixaService
        final DespesaFixa dfSalva = new DespesaFixa(
                101L, "Aluguel Confidencial", new BigDecimal("12345.67"), conta, categoria, "Obs DF Secreta", LocalDate.of(2025, 1, 1));
        when(despesaFixaRepository.saveAndFlush(any(DespesaFixa.class))).thenReturn(dfSalva);
        when(despesaFixaRepository.findById(101L)).thenReturn(Optional.of(dfSalva));
        when(despesaFixaRepository.existsById(101L)).thenReturn(true);

        final DespesaFixaService despesaFixaService = new DespesaFixaService(despesaFixaRepository, contaRepository, categoriaRepository);
        final DespesaFixaRequestDTO dfDto = new DespesaFixaRequestDTO(
                "Aluguel Confidencial", new BigDecimal("12345.67"), new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs DF Secreta", LocalDate.of(2025, 1, 1));
        despesaFixaService.criar(dfDto);
        despesaFixaService.atualizar(101L, dfDto);
        despesaFixaService.excluir(101L);

        assertThatThrownBy(() -> despesaFixaService.criar(new DespesaFixaRequestDTO(
                "Aluguel Confidencial", new BigDecimal("12345.67"), new IdReferenceDTO(99L), new IdReferenceDTO(20L), "Obs DF Secreta", LocalDate.of(2025, 1, 1))))
                .isInstanceOf(AccessDeniedException.class);

        // ReceitaFixaService
        final ReceitaFixa rfSalva = new ReceitaFixa(
                202L, "Salario Confidencial", new BigDecimal("87654.32"), conta, categoria, "Obs RF Secreta", LocalDate.of(2025, 1, 1));
        when(receitaFixaRepository.saveAndFlush(any(ReceitaFixa.class))).thenReturn(rfSalva);
        when(receitaFixaRepository.findById(202L)).thenReturn(Optional.of(rfSalva));
        when(receitaFixaRepository.existsById(202L)).thenReturn(true);

        final ReceitaFixaService receitaFixaService = new ReceitaFixaService(receitaFixaRepository, contaRepository, categoriaRepository);
        final ReceitaFixaRequestDTO rfDto = new ReceitaFixaRequestDTO(
                "Salario Confidencial", new BigDecimal("87654.32"), new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs RF Secreta", LocalDate.of(2025, 1, 1));
        receitaFixaService.criar(rfDto);
        receitaFixaService.atualizar(202L, rfDto);
        receitaFixaService.excluir(202L);

        assertThatThrownBy(() -> receitaFixaService.criar(new ReceitaFixaRequestDTO(
                "Salario Confidencial", new BigDecimal("87654.32"), new IdReferenceDTO(10L), new IdReferenceDTO(98L), "Obs RF Secreta", LocalDate.of(2025, 1, 1))))
                .isInstanceOf(AccessDeniedException.class);

        // DespesaVariavelService
        final DespesaVariavel dvSalva = new DespesaVariavel(
                303L, "Compra Cartao Secreta", "Joalheria Confidencial", LocalDate.of(2025, 2, 10),
                new BigDecimal("4567.89"), 3, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 4, 1),
                categoria, conta, "Obs DV Secreta");
        when(despesaVariavelRepository.saveAndFlush(any(DespesaVariavel.class))).thenReturn(dvSalva);
        when(despesaVariavelRepository.findById(303L)).thenReturn(Optional.of(dvSalva));
        when(despesaVariavelRepository.existsById(303L)).thenReturn(true);

        final DespesaVariavelService despesaVariavelService = new DespesaVariavelService(
                despesaVariavelRepository, contaRepository, categoriaRepository);
        final DespesaVariavelRequestDTO dvDto = new DespesaVariavelRequestDTO(
                "Compra Cartao Secreta", "Joalheria Confidencial", LocalDate.of(2025, 2, 10),
                new BigDecimal("4567.89"), 3, LocalDate.of(2025, 2, 1),
                new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs DV Secreta");
        despesaVariavelService.criar(dvDto);
        despesaVariavelService.atualizar(303L, dvDto);
        despesaVariavelService.excluir(303L);

        assertThatThrownBy(() -> despesaVariavelService.criar(new DespesaVariavelRequestDTO(
                "Compra Cartao Secreta", "Joalheria Confidencial", LocalDate.of(2025, 2, 10),
                new BigDecimal("4567.89"), 3, LocalDate.of(2025, 2, 1),
                new IdReferenceDTO(99L), new IdReferenceDTO(20L), "Obs DV Secreta")))
                .isInstanceOf(AccessDeniedException.class);

        // ReceitaVariavelService
        final ReceitaVariavel rvSalva = new ReceitaVariavel(
                404L, "Consultoria Confidencial", new BigDecimal("9876.54"), 2,
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 1), categoria, conta, "Obs RV Secreta");
        when(receitaVariavelRepository.saveAndFlush(any(ReceitaVariavel.class))).thenReturn(rvSalva);
        when(receitaVariavelRepository.findById(404L)).thenReturn(Optional.of(rvSalva));
        when(receitaVariavelRepository.existsById(404L)).thenReturn(true);

        final ReceitaVariavelService receitaVariavelService = new ReceitaVariavelService(
                receitaVariavelRepository, contaRepository, categoriaRepository);
        final ReceitaVariavelRequestDTO rvDto = new ReceitaVariavelRequestDTO(
                "Consultoria Confidencial", new BigDecimal("9876.54"), 2, LocalDate.of(2025, 2, 1),
                new IdReferenceDTO(10L), new IdReferenceDTO(20L), "Obs RV Secreta");
        receitaVariavelService.criar(rvDto);
        receitaVariavelService.atualizar(404L, rvDto);
        receitaVariavelService.excluir(404L);

        assertThatThrownBy(() -> receitaVariavelService.criar(new ReceitaVariavelRequestDTO(
                "Consultoria Confidencial", new BigDecimal("9876.54"), 2, LocalDate.of(2025, 2, 1),
                new IdReferenceDTO(10L), new IdReferenceDTO(98L), "Obs RV Secreta")))
                .isInstanceOf(AccessDeniedException.class);

        // ConsolidacaoService
        when(contaRepository.findByUsuarioId("user-fin-1")).thenReturn(List.of(conta));
        when(categoriaRepository.findByUsuarioIdOrderByDescricaoAsc("user-fin-1")).thenReturn(List.of(categoria));
        when(despesaFixaRepository.findByUsuarioId("user-fin-1")).thenReturn(List.of(dfSalva));
        when(receitaFixaRepository.findByUsuarioId("user-fin-1")).thenReturn(List.of(rfSalva));
        when(despesaVariavelRepository.findByUsuarioId("user-fin-1")).thenReturn(List.of(dvSalva));
        when(receitaVariavelRepository.findByUsuarioId("user-fin-1")).thenReturn(List.of(rvSalva));

        final ConsolidacaoService consolidacaoService = new ConsolidacaoService(
                contaRepository, categoriaRepository, receitaFixaRepository,
                receitaVariavelRepository, despesaFixaRepository, despesaVariavelRepository);
        consolidacaoService.calcularConsolidacaoPorConta("user-fin-1", "2025-03");
        consolidacaoService.calcularConsolidacaoPorCategoria("user-fin-1", "2025-03");

        final List<ILoggingEvent> events = listAppender.list;

        assertThat(events)
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("Operação de cadastro concluída: entidade=Conta operacao=CRIAR id=10"))
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("Operação de cadastro concluída: entidade=Conta operacao=ATUALIZAR id=10"))
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("Operação de cadastro concluída: entidade=Conta operacao=EXCLUIR id=10"))
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("Operação de cadastro concluída: entidade=Categoria operacao=CRIAR id=20"))
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("Operação financeira concluída: entidade=DespesaFixa operacao=CRIAR id=101 contaId=10 categoriaId=20"))
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("Operação financeira concluída: entidade=ReceitaFixa operacao=CRIAR id=202 contaId=10 categoriaId=20"))
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("Operação financeira concluída: entidade=DespesaVariavel operacao=CRIAR id=303 contaId=10 categoriaId=20"))
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("Operação financeira concluída: entidade=ReceitaVariavel operacao=CRIAR id=404 contaId=10 categoriaId=20"))
                .anyMatch(e -> e.getLevel() == Level.WARN && e.getFormattedMessage().contains("entidade=DespesaFixa") && e.getFormattedMessage().contains("contaId=99"))
                .anyMatch(e -> e.getLevel() == Level.WARN && e.getFormattedMessage().contains("entidade=ReceitaFixa") && e.getFormattedMessage().contains("categoriaId=98"))
                .anyMatch(e -> e.getLevel() == Level.DEBUG && e.getFormattedMessage().contains("entidade=ConsolidacaoPorConta") && e.getFormattedMessage().contains("mesAno=2025-03") && e.getFormattedMessage().contains("itensProcessados=4") && e.getFormattedMessage().contains("duracaoMs="))
                .anyMatch(e -> e.getLevel() == Level.DEBUG && e.getFormattedMessage().contains("entidade=ConsolidacaoPorCategoria") && e.getFormattedMessage().contains("mesAno=2025-03") && e.getFormattedMessage().contains("itensProcessados=2") && e.getFormattedMessage().contains("duracaoMs="));

        final List<String> forbiddenValues = List.of(
                "Conta Secreta VIP",
                "Obs Conta Confidencial",
                "Categoria Secreta VIP",
                "Obs Categoria Confidencial",
                "Aluguel Confidencial",
                "Obs DF Secreta",
                "12345.67",
                "Salario Confidencial",
                "Obs RF Secreta",
                "87654.32",
                "Compra Cartao Secreta",
                "Joalheria Confidencial",
                "Obs DV Secreta",
                "4567.89",
                "Consultoria Confidencial",
                "Obs RV Secreta",
                "9876.54",
                "outro-tenant"
        );

        for (final ILoggingEvent event : events) {
            for (final String forbidden : forbiddenValues) {
                assertThat(event.getFormattedMessage()).doesNotContain(forbidden);
            }
        }
        for (final String forbidden : forbiddenValues) {
            assertThat(output.getAll()).doesNotContain(forbidden);
        }
    }

    private String gerarTokenExpirado(final String userId) {
        final SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis() - 100_000))
                .expiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(key)
                .compact();
    }

    private String extrairValorCookieSessao(final String setCookieHeader) {
        final String prefix = "AUREUS_SESSION=";
        final int idx = setCookieHeader == null ? -1 : setCookieHeader.indexOf(prefix);
        if (idx < 0) {
            return "";
        }
        final int start = idx + prefix.length();
        final int end = setCookieHeader.indexOf(';', start);
        return end >= 0 ? setCookieHeader.substring(start, end) : setCookieHeader.substring(start);
    }
}

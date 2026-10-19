package com.guilhermepagio.aureus.backend.security;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.guilhermepagio.aureus.backend.AbstractIntegrationTest;
import com.guilhermepagio.aureus.backend.domain.dto.ContaRequestDTO;
import com.guilhermepagio.aureus.backend.domain.dto.ContaResponseDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class SecurityIT extends AbstractIntegrationTest {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityIT(
            final MockMvc mockMvc,
            final JwtUtil jwtUtil,
            @Value("${jwt.secret}") final String jwtSecret,
            final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        super(mockMvc, jwtUtil, jwtSecret);
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Test
    @DisplayName("Requisição para /api/** sem cookie AUREUS_SESSION deve retornar 401 Unauthorized")
    void requisicaoSemCookieDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/contas"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/despesas-fixas"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/contas").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Requisição para /api/** com token expirado deve retornar 401 e limpar cookie com todos os atributos")
    void requisicaoComTokenExpiradoDeveRetornar401ELimparCookie() throws Exception {
        mockMvc.perform(get("/api/contas")
                .secure(false)
                .cookie(expiredAuthCookie("user-expirado")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString("AUREUS_SESSION=;"),
                        containsString("Max-Age=0"),
                        containsString("Path=/"),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax"),
                        not(containsString("; Secure"))
                ))));

        mockMvc.perform(get("/api/contas")
                .secure(true)
                .cookie(expiredAuthCookie("user-expirado")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString("AUREUS_SESSION=;"),
                        containsString("Max-Age=0"),
                        containsString("Path=/"),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax"),
                        containsString("; Secure")
                ))));
    }

    @Test
    @DisplayName("Requisição para /api/** com token inválido/malformado deve retornar 401 e limpar cookie com todos os atributos")
    void requisicaoComTokenInvalidoDeveRetornar401ELimparCookie() throws Exception {
        mockMvc.perform(get("/api/contas")
                .secure(false)
                .cookie(invalidAuthCookie()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString("AUREUS_SESSION=;"),
                        containsString("Max-Age=0"),
                        containsString("Path=/"),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax"),
                        not(containsString("; Secure"))
                ))));

        mockMvc.perform(get("/api/contas")
                .secure(true)
                .cookie(invalidAuthCookie()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString("AUREUS_SESSION=;"),
                        containsString("Max-Age=0"),
                        containsString("Path=/"),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax"),
                        containsString("; Secure")
                ))));
    }

    @Test
    @DisplayName("Requisição para /api/** com token válido deve retornar 200 OK")
    void requisicaoComTokenValidoDeveRetornar200() throws Exception {
        mockMvc.perform(get("/api/contas")
                .cookie(authCookie("user-valido")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Requisição POST autenticada sem token CSRF deve retornar 403 Forbidden e não persistir dados")
    void requisicaoMutanteSemCsrfDeveRetornar403() throws Exception {
        final String tenant = "user-csrf-post-" + UUID.randomUUID();

        mockMvc.perform(post("/api/contas")
                .cookie(authCookie(tenant))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"descricao\":\"Teste CSRF\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/contas")
                .cookie(authCookie(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Requisições PUT e DELETE autenticadas sem token CSRF devem retornar 403 Forbidden e manter registro intacto")
    void requisicoesPutEDeleteSemCsrfDevemRetornar403EManterRegistroIntacto() throws Exception {
        final String tenant = "user-csrf-put-del-" + UUID.randomUUID();
        final String descricaoOriginal = "Conta Original";
        final String observacoesOriginais = "Obs Original";

        final ContaRequestDTO criacao = new ContaRequestDTO(descricaoOriginal, observacoesOriginais);
        final MvcResult result = mockMvc.perform(post("/api/contas")
                .cookie(authCookie(tenant))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criacao)))
                .andExpect(status().isOk())
                .andReturn();
        final ContaResponseDTO contaCriada = objectMapper.readValue(
                result.getResponse().getContentAsString(), ContaResponseDTO.class);

        final ContaRequestDTO alteracaoSemCsrf = new ContaRequestDTO("Conta Alterada", "Obs Alterada");
        mockMvc.perform(put("/api/contas/" + contaCriada.id())
                .cookie(authCookie(tenant))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alteracaoSemCsrf)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/contas/" + contaCriada.id())
                .cookie(authCookie(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value(descricaoOriginal))
                .andExpect(jsonPath("$.observacoes").value(observacoesOriginais));

        mockMvc.perform(delete("/api/contas/" + contaCriada.id())
                .cookie(authCookie(tenant)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/contas/" + contaCriada.id())
                .cookie(authCookie(tenant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value(descricaoOriginal))
                .andExpect(jsonPath("$.observacoes").value(observacoesOriginais));
    }

    @Test
    @DisplayName("Endpoint /api/auth/me deve retornar 401 para anônimo e 200 para autenticado; /api/auth/logout deve exigir CSRF e expirar cookie com todos os atributos em HTTP e HTTPS")
    void endpointsDeAuthDevemFuncionarCorretamente() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me").cookie(authCookie("1")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/logout").cookie(authCookie("1")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/logout")
                .secure(false)
                .cookie(authCookie("1"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString("AUREUS_SESSION=;"),
                        containsString("Max-Age=0"),
                        containsString("Path=/"),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax"),
                        not(containsString("; Secure"))
                ))));

        mockMvc.perform(post("/api/auth/logout")
                .secure(true)
                .cookie(authCookie("1"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString("AUREUS_SESSION=;"),
                        containsString("Max-Age=0"),
                        containsString("Path=/"),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax"),
                        containsString("; Secure")
                ))));
    }

    @Test
    @DisplayName("Allowlist pública deve preservar fluxo OAuth2 e /actuator/health sem detalhes, negando /actuator/env e rotas fora da allowlist")
    void allowlistPublicaEPoliticaActuatorDevemNegarRotasNaoAutorizadas() throws Exception {
        mockMvc.perform(get("/api/oauth2/authorization/google"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("accounts.google.com")));

        mockMvc.perform(get("/login/oauth2/code/google"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/login?error=true")));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        mockMvc.perform(get("/actuator/health").cookie(authCookie("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        mockMvc.perform(get("/actuator"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator").cookie(authCookie("1")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/env").cookie(authCookie("1")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/beans").cookie(authCookie("1")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/login"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/login").cookie(authCookie("1")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/oauth2/invalido"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/oauth2/invalido").cookie(authCookie("1")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/rota-inexistente"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/rota-inexistente").cookie(authCookie("1")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/").cookie(authCookie("1")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OAuth2LoginSuccessHandler deve emitir cookie AUREUS_SESSION com HttpOnly, Path=/, SameSite=Lax, Max-Age coerente com jwt.expiration e Secure condicionado ao transporte")
    void oauth2LoginSuccessHandlerDeveEmitirCookieComAtributosDeSegurancaEmHttpEHttps() throws Exception {
        final String googleSub = "google-sub-" + UUID.randomUUID();
        final OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "sub", googleSub,
                        "email", "usuario@aureus.test",
                        "name", "Usuario Teste"
                ),
                "sub"
        );
        final OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                oAuth2User, oAuth2User.getAuthorities(), "google");
        final long expectedMaxAgeSeconds = jwtUtil.getExpiration() / 1000;

        final MockHttpServletRequest httpRequest = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        httpRequest.setSecure(false);
        final MockHttpServletResponse httpResponse = new MockHttpServletResponse();

        oAuth2LoginSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authToken);

        assertThat(httpResponse.getStatus()).isEqualTo(302);
        assertThat(httpResponse.getRedirectedUrl()).isEqualTo("http://localhost:5173");

        final String setCookieHttp = httpResponse.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHttp)
                .isNotNull()
                .contains("AUREUS_SESSION=")
                .contains("HttpOnly")
                .contains("Path=/")
                .contains("SameSite=Lax")
                .contains("Max-Age=" + expectedMaxAgeSeconds)
                .doesNotContain("; Secure");

        final String jwtHttp = extrairValorCookieSessao(setCookieHttp);
        assertThat(jwtHttp).isNotBlank();
        jwtUtil.validateToken(jwtHttp);
        assertThat(jwtUtil.getSubject(jwtHttp)).isNotBlank();

        final MockHttpServletRequest httpsRequest = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        httpsRequest.setSecure(true);
        final MockHttpServletResponse httpsResponse = new MockHttpServletResponse();

        oAuth2LoginSuccessHandler.onAuthenticationSuccess(httpsRequest, httpsResponse, authToken);

        assertThat(httpsResponse.getStatus()).isEqualTo(302);
        assertThat(httpsResponse.getRedirectedUrl()).isEqualTo("http://localhost:5173");

        final String setCookieHttps = httpsResponse.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHttps)
                .isNotNull()
                .contains("AUREUS_SESSION=")
                .contains("HttpOnly")
                .contains("Path=/")
                .contains("SameSite=Lax")
                .contains("Max-Age=" + expectedMaxAgeSeconds)
                .contains("; Secure");

        final String jwtHttps = extrairValorCookieSessao(setCookieHttps);
        assertThat(jwtHttps).isNotBlank();
        jwtUtil.validateToken(jwtHttps);
        assertThat(jwtHttps).isNotBlank();
        assertThat(jwtUtil.getSubject(jwtHttps)).isEqualTo(jwtUtil.getSubject(jwtHttp));
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


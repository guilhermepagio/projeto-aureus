package com.guilhermepagio.aureus.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import com.guilhermepagio.aureus.backend.AbstractIntegrationTest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityIT extends AbstractIntegrationTest {

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
    @DisplayName("Requisição para /api/** com token expirado deve retornar 401 e limpar cookie")
    void requisicaoComTokenExpiradoDeveRetornar401ELimparCookie() throws Exception {
        mockMvc.perform(get("/api/contas")
                .cookie(expiredAuthCookie("user-expirado")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("AUREUS_SESSION="))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("Max-Age=0"))));
    }

    @Test
    @DisplayName("Requisição para /api/** com token inválido/malformado deve retornar 401 e limpar cookie")
    void requisicaoComTokenInvalidoDeveRetornar401ELimparCookie() throws Exception {
        mockMvc.perform(get("/api/contas")
                .cookie(invalidAuthCookie()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("AUREUS_SESSION="))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(containsString("Max-Age=0"))));
    }

    @Test
    @DisplayName("Requisição para /api/** com token válido deve retornar 200 OK")
    void requisicaoComTokenValidoDeveRetornar200() throws Exception {
        mockMvc.perform(get("/api/contas")
                .cookie(authCookie("user-valido")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Requisição mutante autenticada sem token CSRF deve retornar 403 Forbidden")
    void requisicaoMutanteSemCsrfDeveRetornar403() throws Exception {
        mockMvc.perform(post("/api/contas")
                .cookie(authCookie("user-csrf"))
                .contentType("application/json")
                .content("{\"descricao\":\"Teste CSRF\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Endpoint /api/auth/me deve retornar 401 para anônimo e 200 para autenticado; /api/auth/logout deve ser acessível")
    void endpointsDeAuthDevemFuncionarCorretamente() throws Exception {
        // Anônimo no /me retorna 401
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        // Autenticado no /me retorna 200
        mockMvc.perform(get("/api/auth/me").cookie(authCookie("1")))
                .andExpect(status().isOk());

        // Logout público retorna 200 e Set-Cookie de limpeza
        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }
}

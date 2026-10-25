package com.guilhermepagio.aureus.backend.security;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import jakarta.servlet.ServletException;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2LoginFailureHandlerTest {

    @Test
    @DisplayName("Deve redirecionar para a URL do frontend configurada com parâmetro error=true")
    void deveRedirecionarParaFrontendConfigurado() throws IOException, ServletException {
        final OAuth2LoginFailureHandler handler = new OAuth2LoginFailureHandler("https://app.aureus.com");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("Falha na autenticação"));

        assertThat(response.getRedirectedUrl()).isEqualTo("https://app.aureus.com/login?error=true");
    }

    @Test
    @DisplayName("Deve remover barra no final da URL configurada antes de adicionar /login?error=true")
    void deveNormalizarBarraFinal() throws IOException, ServletException {
        final OAuth2LoginFailureHandler handler = new OAuth2LoginFailureHandler("http://localhost:5173/");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("Falha"));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/login?error=true");
    }

    @Test
    @DisplayName("Deve usar fallback seguro quando frontendUrl for nulo ou em branco")
    void deveUsarFallbackQuandoNuloOuEmBranco() throws IOException, ServletException {
        final OAuth2LoginFailureHandler handlerNulo = new OAuth2LoginFailureHandler(null);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handlerNulo.onAuthenticationFailure(request, response, new BadCredentialsException("Falha"));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/login?error=true");

        final OAuth2LoginFailureHandler handlerBranco = new OAuth2LoginFailureHandler("   ");
        final MockHttpServletResponse responseBranco = new MockHttpServletResponse();

        handlerBranco.onAuthenticationFailure(request, responseBranco, new BadCredentialsException("Falha"));

        assertThat(responseBranco.getRedirectedUrl()).isEqualTo("http://localhost:5173/login?error=true");
    }
}

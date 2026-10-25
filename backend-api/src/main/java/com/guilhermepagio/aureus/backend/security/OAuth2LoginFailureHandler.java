package com.guilhermepagio.aureus.backend.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String frontendUrl;

    public OAuth2LoginFailureHandler(@Value("${app.frontend.url}") final String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationFailure(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException exception) throws IOException, ServletException {
        final String base = (frontendUrl != null && !frontendUrl.isBlank())
                ? frontendUrl.replaceAll("/+$", "")
                : "http://localhost:5173";
        getRedirectStrategy().sendRedirect(request, response, base + "/login?error=true");
    }
}

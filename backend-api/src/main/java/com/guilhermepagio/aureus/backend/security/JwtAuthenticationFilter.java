package com.guilhermepagio.aureus.backend.security;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_TENANT_ID = "tenantId";
    private static final String ANONYMOUS_TENANT = "anon";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {

        try {
            TenantContext.clear();
            SecurityContextHolder.clearContext();

            final String requestId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(MDC_REQUEST_ID, requestId);
            MDC.put(MDC_TENANT_ID, ANONYMOUS_TENANT);

            final String method = request != null && request.getMethod() != null && !request.getMethod().isBlank()
                    ? request.getMethod()
                    : "UNKNOWN";
            final String uri = request != null && request.getRequestURI() != null && !request.getRequestURI().isBlank()
                    ? request.getRequestURI()
                    : "/";

            final Cookie cookie = request != null ? WebUtils.getCookie(request, "AUREUS_SESSION") : null;
            if (cookie != null) {
                final String token = cookie.getValue();
                try {
                    jwtUtil.validateToken(token);
                    final String subjectId = jwtUtil.getSubject(token);
                    if (subjectId == null || subjectId.isBlank()) {
                        throw new IllegalArgumentException("Subject ausente no token JWT");
                    }
                    final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            subjectId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    TenantContext.setTenantId(subjectId);
                    MDC.put(MDC_TENANT_ID, subjectId);
                } catch (final Exception e) {
                    TenantContext.clear();
                    SecurityContextHolder.clearContext();
                    MDC.put(MDC_TENANT_ID, ANONYMOUS_TENANT);

                    final String motivo = (e instanceof ExpiredJwtException) ? "token_expirado" : "token_invalido";
                    log.warn(
                            "Token JWT rejeitado na autenticação: method={} uri={} resultado=rejeitado motivo={} excecao={}",
                            method, uri, motivo, e.getClass().getSimpleName());

                    final ResponseCookie clearCookie = ResponseCookie.from("AUREUS_SESSION", "")
                            .maxAge(0)
                            .path("/")
                            .httpOnly(true)
                            .secure(request.isSecure())
                            .sameSite("Lax")
                            .build();
                    response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
                }
            } else {
                log.debug(
                        "Requisição sem cookie de sessão: method={} uri={} resultado=anonimo motivo=cookie_ausente",
                        method, uri);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_TENANT_ID);
        }
    }
}

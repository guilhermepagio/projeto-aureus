package com.guilhermepagio.aureus.backend.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            final Cookie cookie = WebUtils.getCookie(request, "AUREUS_SESSION");
            if (cookie != null) {
                final String token = cookie.getValue();
                try {
                    jwtUtil.validateToken(token);
                    final String subjectId = jwtUtil.getSubject(token);
                    final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            subjectId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    TenantContext.setTenantId(subjectId);
                } catch (final Exception e) {
                    final ResponseCookie clearCookie = ResponseCookie.from("AUREUS_SESSION", "")
                            .maxAge(0)
                            .path("/")
                            .httpOnly(true)
                            .secure(request.isSecure())
                            .sameSite("Lax")
                            .build();
                    response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
                }
            }
            
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}

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

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            Cookie cookie = WebUtils.getCookie(request, "AUREUS_SESSION");
            if (cookie != null) {
                String token = cookie.getValue();
                try {
                    jwtUtil.validateToken(token);
                    String subjectId = jwtUtil.getSubject(token);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            subjectId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    TenantContext.setTenantId(subjectId);
                } catch (Exception e) {
                    ResponseCookie clearCookie = ResponseCookie.from("AUREUS_SESSION", "")
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

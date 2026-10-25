package com.guilhermepagio.aureus.backend.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guilhermepagio.aureus.backend.domain.Usuario;
import com.guilhermepagio.aureus.backend.domain.dto.UserProfileResponseDTO;
import com.guilhermepagio.aureus.backend.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> me(final Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.debug("Consulta de sessão não autenticada: method=GET uri=/api/auth/me resultado=nao_autenticado motivo=sessao_ausente_ou_invalida");
            return ResponseEntity.status(401).build();
        }
        
        final String subjectId = String.valueOf(authentication.getPrincipal());
        final Optional<Usuario> usuarioOpt = usuarioRepository.findById(Long.valueOf(subjectId));
        final String fotoPerfil = usuarioOpt.map((final Usuario usuario) -> usuario.getFotoPerfil()).orElse(null);
        
        return ResponseEntity.ok(new UserProfileResponseDTO(subjectId, fotoPerfil));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletRequest request) {
        final ResponseCookie cookie = ResponseCookie.from("AUREUS_SESSION", "")
                .httpOnly(true)
                .secure(request.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        final String method = request != null && request.getMethod() != null && !request.getMethod().isBlank()
                ? request.getMethod()
                : "POST";
        final String uri = request != null && request.getRequestURI() != null && !request.getRequestURI().isBlank()
                ? request.getRequestURI()
                : "/api/auth/logout";
        log.info("Sessão encerrada via logout: method={} uri={} resultado=sucesso", method, uri);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}

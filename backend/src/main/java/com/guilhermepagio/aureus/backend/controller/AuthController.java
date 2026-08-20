package com.guilhermepagio.aureus.backend.controller;

import com.guilhermepagio.aureus.backend.domain.Usuario;
import com.guilhermepagio.aureus.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(final Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        
        final String subjectId = String.valueOf(authentication.getPrincipal());
        final Optional<Usuario> usuarioOpt = usuarioRepository.findById(Long.valueOf(subjectId));
        
        final Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("subjectId", subjectId);
        usuarioOpt.ifPresent(usuario -> responseBody.put("fotoPerfil", usuario.getFotoPerfil()));
        
        return ResponseEntity.ok(responseBody);
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
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}

package com.guilhermepagio.aureus.backend.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.guilhermepagio.aureus.backend.domain.Usuario;
import com.guilhermepagio.aureus.backend.repository.UsuarioRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
            final UsuarioRepository usuarioRepository,
            final JwtUtil jwtUtil,
            @Value("${app.frontend.url}") final String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication) throws IOException, ServletException {
        final OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        final String googleSubjectId = oAuth2User.getAttribute("sub");
        final String email = oAuth2User.getAttribute("email");
        final String name = oAuth2User.getAttribute("name");
        final String picture = oAuth2User.getAttribute("picture");

        final Usuario usuario = usuarioRepository.findByGoogleSubjectId(googleSubjectId)
                .orElseGet(() -> {
                    final Usuario newUser = new Usuario();
                    newUser.setGoogleSubjectId(googleSubjectId);
                    return newUser;
                });
        
        usuario.setEmail(email);
        usuario.setNome(name);
        usuario.setFotoPerfil(picture);
        usuarioRepository.save(usuario);

        final String token = jwtUtil.generateToken(usuario.getId().toString());

        final ResponseCookie cookie = ResponseCookie.from("AUREUS_SESSION", token)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtUtil.getExpiration() / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        final String method = request != null && request.getMethod() != null && !request.getMethod().isBlank()
                ? request.getMethod()
                : "GET";
        final String uri = request != null && request.getRequestURI() != null && !request.getRequestURI().isBlank()
                ? request.getRequestURI()
                : "/login/oauth2/code/google";
        log.info("Login OAuth2 concluído: method={} uri={} resultado=sucesso usuarioId={}",
                method, uri, usuario.getId());

        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}

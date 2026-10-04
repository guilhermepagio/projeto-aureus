package com.guilhermepagio.aureus.backend.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.guilhermepagio.aureus.backend.domain.Usuario;
import com.guilhermepagio.aureus.backend.repository.UsuarioRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String googleSubjectId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        Usuario usuario = usuarioRepository.findByGoogleSubjectId(googleSubjectId)
                .orElseGet(() -> {
                    Usuario newUser = new Usuario();
                    newUser.setGoogleSubjectId(googleSubjectId);
                    return newUser;
                });
        
        usuario.setEmail(email);
        usuario.setNome(name);
        usuario.setFotoPerfil(picture);
        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getId().toString());

        ResponseCookie cookie = ResponseCookie.from("AUREUS_SESSION", token)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtUtil.getExpiration() / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}

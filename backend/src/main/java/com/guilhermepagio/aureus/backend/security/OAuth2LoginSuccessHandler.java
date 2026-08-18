package com.guilhermepagio.aureus.backend.security;

import com.guilhermepagio.aureus.backend.domain.Usuario;
import com.guilhermepagio.aureus.backend.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

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

        String token = jwtUtil.generateToken(usuario.getGoogleSubjectId());

        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("AUREUS_SESSION", token)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(86400)
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/");
    }
}

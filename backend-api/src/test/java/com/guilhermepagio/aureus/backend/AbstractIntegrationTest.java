package com.guilhermepagio.aureus.backend;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.guilhermepagio.aureus.backend.security.JwtUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USER", postgres::getUsername);
        registry.add("DB_PASS", postgres::getPassword);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("GOOGLE_CLIENT_ID", () -> "fake-google-client-id");
        registry.add("GOOGLE_CLIENT_SECRET", () -> "fake-google-client-secret");
        registry.add("JWT_SECRET", () -> "8f8a8473822a4d70b33b006a6b82b9937b8b4abcf35e408dbb0a1d4a04791e84");
        registry.add("FRONTEND_URL", () -> "http://localhost:5173");
    }

    protected final MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    protected final JwtUtil jwtUtil;

    private final String jwtSecret;

    protected AbstractIntegrationTest(
            final MockMvc mockMvc,
            final JwtUtil jwtUtil,
            @Value("${jwt.secret}") final String jwtSecret) {
        this.mockMvc = mockMvc;
        this.jwtUtil = jwtUtil;
        this.jwtSecret = jwtSecret;
    }

    protected Cookie authCookie(final String userId) {
        final String token = jwtUtil.generateToken(userId);
        return new Cookie("AUREUS_SESSION", token);
    }

    protected Cookie expiredAuthCookie(final String userId) {
        final SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        final String token = Jwts.builder()
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis() - 100_000))
                .expiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(key)
                .compact();
        return new Cookie("AUREUS_SESSION", token);
    }

    protected Cookie invalidAuthCookie() {
        return new Cookie("AUREUS_SESSION", "invalid-token-content");
    }
}

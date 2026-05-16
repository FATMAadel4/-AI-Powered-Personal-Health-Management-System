package com.example.Notification_Service.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String SECRET_KEY =
            "myverysecuresecretkeythatislongenough1234567890abcdef";

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    private static final List<String> PUBLIC_URLS = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-ui.html",
            "/swagger-resources",
            "/webjars",
            "/ws" // ← WebSocket endpoint مفتوح
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_URLS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        try {
            String token = authHeader.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.parseLong(claims.getSubject());
            String email = (String) claims.get("email");

            request.setAttribute("userId", userId);
            request.setAttribute("email", email);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
        }
    }

    public Long extractUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    public String extractEmail(HttpServletRequest request) {
        return (String) request.getAttribute("email");
    }
}


package com.example.auth_service.springjwt.services;

import com.example.auth_service.springjwt.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "myverysecuresecretkeythatislongenough1234567890abcdef";

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    private final long accessTokenValidity = 24 * 60 * 60 * 1000;

    public String createToken(User user, Map<String, Object> extraClaims) {

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(String.valueOf(user.getId()))  // ✅ ID بدل Email

                .claim("roles", List.of(user.getRole().name()))
                .claim("email", user.getEmail())           // ✅ email كـ claim

                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))

                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseJwtClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String resolveToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    public Claims resolveClaims(HttpServletRequest req) {

        try {

            String token = resolveToken(req);

            if (token != null) {
                return parseJwtClaims(token);
            }

            return null;

        } catch (ExpiredJwtException ex) {

            req.setAttribute("expired", ex.getMessage());
            throw ex;

        } catch (Exception ex) {

            req.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    public boolean isTokenExpired(Date expirationDate)
            throws AuthenticationException {

        return expirationDate.before(new Date());
    }

    public boolean isTokenValid(String accessToken, UserDetails userDetails) {

        Claims claims = parseJwtClaims(accessToken);

        // ✅ الـ subject دلوقتي ID — نقارنه بالـ username من الـ DB
        String userId = claims.getSubject();
        String email = (String) claims.get("email");

        return email.equals(userDetails.getUsername())
                && !isTokenExpired(claims.getExpiration());
    }

    public List<String> getRoles(Claims claims) {

        return claims.get("roles", List.class);
    }
}
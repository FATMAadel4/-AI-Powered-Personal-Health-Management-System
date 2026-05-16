package com.example.auth_service.springjwt.services;

import com.example.auth_service.springjwt.entity.RefreshToken;
import com.example.auth_service.springjwt.entity.User;
import com.example.auth_service.springjwt.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    // 7 أيام
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // ============================================================
    // اعمل Refresh Token جديد واحفظه في الـ DB
    // ============================================================
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString()) // قيمة عشوائية مميزة
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // ============================================================
    // تحقق إن الـ Refresh Token صح قبل ما تعمل access token جديد
    // ============================================================
    public RefreshToken verifyRefreshToken(String token) {

        // 1. موجود في الـ DB؟
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // 2. اتلغى بـ logout؟
        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked, please login again");
        }

        // 3. انتهت مدته؟
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken); // امسحه ينضّف الـ DB
            throw new RuntimeException("Refresh token has expired, please login again");
        }

        return refreshToken;
    }

    // ============================================================
    // لما اليوزر يعمل logout — الغِ كل التوكنز بتاعته
    // ============================================================
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }
}
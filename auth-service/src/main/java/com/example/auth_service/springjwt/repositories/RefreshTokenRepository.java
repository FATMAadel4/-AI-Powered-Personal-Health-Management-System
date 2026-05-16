package com.example.auth_service.springjwt.repositories;

import com.example.auth_service.springjwt.entity.RefreshToken;
import com.example.auth_service.springjwt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // جيب التوكن من الـ DB بالقيمة بتاعته
    Optional<RefreshToken> findByToken(String token);

    // لما اليوزر يعمل logout — الغِ كل التوكنز بتاعته
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllUserTokens(User user);
}
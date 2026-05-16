package com.example.auth_service.springjwt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // القيمة العشوائية — دي اللي بتتبعت للـ client
    @Column(nullable = false, unique = true)
    private String token;

    // بينتهي بعد 7 أيام
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // اتلغى بـ logout؟
    private boolean revoked = false;

    // بتاع مين؟
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
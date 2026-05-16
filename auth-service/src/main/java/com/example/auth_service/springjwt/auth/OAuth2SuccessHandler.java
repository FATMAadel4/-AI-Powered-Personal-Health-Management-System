package com.example.auth_service.springjwt.auth;

import com.example.auth_service.springjwt.entity.Role;
import com.example.auth_service.springjwt.entity.User;
import com.example.auth_service.springjwt.repositories.UserRepository;
import com.example.auth_service.springjwt.services.JwtService;
import com.example.auth_service.springjwt.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // ============================================================
        // STEP 1: جيب بيانات اليوزر اللي رجعت من Google
        // ============================================================
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");  // الإيميل من Google
        String name  = oAuth2User.getAttribute("name");   // الاسم من Google

        // ============================================================
        // STEP 2: اتحقق لو اليوزر ده سجّل قبل كده أو لأ
        // ============================================================
        User user = userRepository.findUserByEmail(email);

        if (user == null) {
            // ← أول مرة يسجل بـ Google — اعمله account تلقائي
            user = User.builder()
                    .email(email)
                    .password("")          // مفيش password لأنه بيسجل بـ Google
                    .phoneNumber("")       // مفيش phone
                    .role(Role.USER)       // الـ default role
                    .enabled(true)         // مش محتاج OTP — Google بتتحقق عنّا
                    .build();
            userRepository.save(user);
        }

        // ============================================================
        // STEP 3: اعمل Access Token + Refresh Token زي الـ login العادي
        // ============================================================
        String accessToken  = jwtService.createToken(user, new HashMap<>());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        // ============================================================
        // STEP 4: ابعت التوكنز في الـ Response كـ JSON
        // ============================================================
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{"
                        + "\"accessToken\":\""  + accessToken  + "\","
                        + "\"refreshToken\":\"" + refreshToken + "\","
                        + "\"email\":\""        + email        + "\""
                        + "}"
        );
    }
}
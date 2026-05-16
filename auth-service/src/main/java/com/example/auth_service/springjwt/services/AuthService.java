package com.example.auth_service.springjwt.services;

import com.example.auth_service.springjwt.entity.RefreshToken;
import com.example.auth_service.springjwt.entity.Token;
import com.example.auth_service.springjwt.entity.TokenType;
import com.example.auth_service.springjwt.entity.User;
import com.example.auth_service.springjwt.model.response.AuthenticationResponse;
import com.example.auth_service.springjwt.model.request.LoginRequest;
import com.example.auth_service.springjwt.model.request.RegisterRequest;
import com.example.auth_service.springjwt.model.response.UserTokenResponse;
import com.example.auth_service.springjwt.repositories.RefreshTokenRepository;
import com.example.auth_service.springjwt.repositories.TokenRepository;
import com.example.auth_service.springjwt.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TextbeltOtpService textbeltOtpService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;


    public AuthenticationResponse login(LoginRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail());
        if (user == null) {
            return new AuthenticationResponse(null, null, "Invalid email or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthenticationResponse(null, null, "Invalid email or password");
        }

        if (!user.isEnabled()) {
            textbeltOtpService.sendOtp(user.getEmail());
            return new AuthenticationResponse(null, null, "Account not activated. New OTP sent to your email.");
        }

        Map<String, Object> extraClaims = new HashMap<>();
        String accessToken = jwtService.createToken(user, extraClaims);
        saveUserToken(user, accessToken);

        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthenticationResponse(accessToken, refreshToken, "Login successful");
    }


    public AuthenticationResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        Map<String, Object> extraClaims = new HashMap<>();
        String newAccessToken = jwtService.createToken(user, extraClaims);
        saveUserToken(user, newAccessToken);

        return new AuthenticationResponse(newAccessToken, refreshTokenValue, user.getEmail());
    }


    public AuthenticationResponse logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshTokenService.revokeAllUserTokens(refreshToken.getUser());

        return new AuthenticationResponse("Logged out successfully");
    }


    public AuthenticationResponse register(RegisterRequest request) {
        Optional<User> existingUser = Optional.ofNullable(userRepository.findUserByEmail(request.getEmail()));

        if (existingUser.isPresent()) {
            return new AuthenticationResponse(null, null, "Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);
        textbeltOtpService.sendOtp(savedUser.getEmail()); // ← email

        return new AuthenticationResponse(null, null, "OTP sent to your email");
    }


    public boolean activateAccount(String email, String otpCode) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findUserByEmail(email));

        return userOpt.map(user -> {
            boolean isVerified = textbeltOtpService.verifyOtp(email, otpCode);
            if (isVerified) {
                user.setEnabled(true);
                userRepository.save(user);
                return true;
            }
            return false;
        }).orElse(false);
    }


    public AuthenticationResponse activateAccountWithToken(String email, String otpCode) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findUserByEmail(email));

        return userOpt.map(user -> {
            boolean isVerified = textbeltOtpService.verifyOtp(email, otpCode);
            if (isVerified) {
                user.setEnabled(true);
                userRepository.save(user);

                Map<String, Object> extraClaims = new HashMap<>();
                String accessToken = jwtService.createToken(user, extraClaims);
                saveUserToken(user, accessToken);

                String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

                return new AuthenticationResponse(accessToken, refreshToken, user.getEmail());
            }
            return new AuthenticationResponse("Invalid OTP");
        }).orElse(new AuthenticationResponse("Email not found"));
    }


    public boolean generateForgetPasswordOtp(String email) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findUserByEmail(email));
        return userOpt.map(user -> {
            textbeltOtpService.sendOtp(user.getEmail());
            return true;
        }).orElse(false);
    }


    public boolean changePassword(String email, String otpCode, String newPassword) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findUserByEmail(email));
        return userOpt.map(user -> {
            boolean isVerified = textbeltOtpService.verifyOtp(email, otpCode);
            if (isVerified) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
            return false;
        }).orElse(false);
    }


    public boolean regenerateOtpByEmail(String email) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findUserByEmail(email));
        return userOpt.map(user -> {
            textbeltOtpService.sendOtp(user.getEmail());
            return true;
        }).orElse(false);
    }


    public boolean validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return false;
        String token = authHeader.substring(7);
        return isTokenValid(token);
    }


    private boolean isTokenValid(String token) {
        try {
            Claims claims = jwtService.parseJwtClaims(token);
            String email = claims.getSubject();
            Optional<User> userOpt = Optional.ofNullable(userRepository.findUserByEmail(email));
            return userOpt.isPresent() && !jwtService.isTokenExpired(claims.getExpiration());
        } catch (Exception e) {
            return false;
        }
    }


    public UserTokenResponse validateTokenAndGetUser(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;

            String token = authHeader.substring(7);
            Claims claims = jwtService.parseJwtClaims(token);

            if (jwtService.isTokenExpired(claims.getExpiration())) return null;

            String email = claims.getSubject();
            User user = userRepository.findUserByEmail(email);

            if (user == null || !user.isEnabled()) return null;

            return new UserTokenResponse(user.getId(), user.getEmail(), user.getRole().toString());

        } catch (Exception e) {
            return null;
        }
    }


    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
package com.example.auth_service.springjwt.controllers;

import com.example.auth_service.springjwt.model.response.AuthenticationResponse;
import com.example.auth_service.springjwt.model.request.LoginRequest;
import com.example.auth_service.springjwt.model.request.RefreshTokenRequest;
import com.example.auth_service.springjwt.model.request.RegisterRequest;
import com.example.auth_service.springjwt.model.response.UserTokenResponse;
import com.example.auth_service.springjwt.services.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthenticationResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        AuthenticationResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<AuthenticationResponse> activateAccount(
            @RequestParam String email,
            @RequestParam String otp) {

        AuthenticationResponse response = authService.activateAccountWithToken(email, otp);

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forgetPassword")
    public ResponseEntity<String> forgetPassword(@RequestParam String email) {
        try {
            authService.generateForgetPasswordOtp(email);
            return ResponseEntity.ok("OTP sent to your email");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        try {
            boolean changed = authService.changePassword(email, otp, newPassword);
            if (changed) {
                return ResponseEntity.ok("Password changed successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid OTP or email");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/regenerateOtp")
    public ResponseEntity<String> regenerateOtp(@RequestParam String email) {
        try {
            authService.regenerateOtpByEmail(email);
            return ResponseEntity.ok("New OTP sent to your email");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/checkToken")
    public ResponseEntity<UserTokenResponse> checkToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        UserTokenResponse response = authService.validateTokenAndGetUser(authHeader);

        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @RequestBody RefreshTokenRequest request) {
        try {
            AuthenticationResponse response = authService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthenticationResponse> logout(
            @RequestBody RefreshTokenRequest request) {
        try {
            AuthenticationResponse response = authService.logout(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new AuthenticationResponse(e.getMessage()));
        }
    }
}
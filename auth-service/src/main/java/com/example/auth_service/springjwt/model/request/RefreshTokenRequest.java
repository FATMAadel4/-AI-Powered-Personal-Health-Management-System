package com.example.auth_service.springjwt.model.request;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
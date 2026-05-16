package com.example.auth_service.springjwt.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenResponse {
    private Integer userId;
    private String email;
    private String role;
}
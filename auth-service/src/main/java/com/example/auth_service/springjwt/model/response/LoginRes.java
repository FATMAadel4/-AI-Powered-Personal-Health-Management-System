package com.example.auth_service.springjwt.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class LoginRes {
    private String email;
    private String token;

}

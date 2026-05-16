
package com.example.auth_service.springjwt.model.response;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String message;

    // Used in login(), register()
    public AuthenticationResponse(String accessToken, String refreshToken, String message) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
    }

    // Used in refreshAccessToken(), activateAccountWithToken()
    public AuthenticationResponse(String accessToken, String email) {
        this.accessToken = accessToken;
        this.email = email;
    }

    // Used in logout(), error cases
    public AuthenticationResponse(String message) {
        this.message = message;
    }

    public String getToken() {
        return accessToken;
    }

    public String getMessage() {
        return message;
    }
}
package com.example.auth_service.springjwt.DTO;


public class UpdateUserRequest {
    private String email;
    private String newEmail;
    private String newPassword;

    // getters & setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}


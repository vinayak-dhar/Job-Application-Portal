package com.jobportal.auth.auth_service.dto;

public class JwtResponse {
    private String token;
    private String email;
    private String role;

    // getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

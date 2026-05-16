package com.diskee.diskee_project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTOs {
    
    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        
        @NotBlank @Size(min = 6)
        private String password;
    }
    
    @Data
    public static class RegisterRequest {
        @NotBlank @Email
        private String email;
        
        @NotBlank @Size(min = 6)
        private String password;
        
        @Size(max = 255)
        private String displayName;
    }
    
    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long expiresIn;
    }
    
    @Data
    public static class RefreshTokenRequest {
        @NotBlank
        private String refreshToken;
    }
}
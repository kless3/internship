package com.internship.auth_service.service;

import com.internship.auth_service.dto.*;

public interface AuthService {
    TokenResponse login(LoginRequest loginRequest);
    TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    TokenValidationResponse validateToken(ValidateTokenRequest validateTokenRequest);
    void registerUser(RegisterRequest registerRequest);
    boolean userExists(String login);
}
package com.internship.auth_service.service;

import com.internship.auth_service.dto.LoginRequest;
import com.internship.auth_service.dto.TokenValidationResponse;
import com.internship.auth_service.dto.RefreshTokenRequest;
import com.internship.auth_service.dto.TokenResponse;
import com.internship.auth_service.dto.ValidateTokenRequest;

public interface TokenManagementService {

    TokenResponse login(LoginRequest loginRequest);
    TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    TokenValidationResponse validateToken(ValidateTokenRequest validateTokenRequest);

}
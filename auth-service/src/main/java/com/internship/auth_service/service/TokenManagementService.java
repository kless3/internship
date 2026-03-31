package com.internship.auth_service.service;

import com.internship.auth_service.dto.LoginRequest;
import com.internship.auth_service.dto.TokenValidationResponse;
import com.internship.auth_service.dto.TokenResponse;
import com.internship.auth_service.dto.ValidateTokenRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenManagementService {

    TokenResponse login(LoginRequest loginRequest);
    TokenResponse loginWithRefreshCookie(LoginRequest loginRequest, HttpServletResponse response);
    TokenResponse refreshToken(String refreshToken);
    TokenResponse refreshWithRotatedCookie(String refreshTokenFromCookie, HttpServletResponse response);
    TokenValidationResponse validateToken(ValidateTokenRequest validateTokenRequest);
    String resolveRefreshToken(String refreshTokenFromCookie);
    String buildRefreshCookie(String token);
    String clearRefreshCookie();

}

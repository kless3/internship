package com.internship.auth_service.service;

import com.internship.auth_service.dto.*;

public interface TokenManagementService {

    TokenResponse login(LoginRequest loginRequest);
    TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    TokenValidationResponse validateToken(ValidateTokenRequest validateTokenRequest);

}

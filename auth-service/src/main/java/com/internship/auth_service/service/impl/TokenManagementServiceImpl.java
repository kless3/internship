package com.internship.auth_service.service.impl;

import com.internship.auth_service.dto.*;
import com.internship.auth_service.exception.AuthServiceException;
import com.internship.auth_service.exception.AuthenticationException;
import com.internship.auth_service.exception.InvalidTokenException;
import com.internship.auth_service.exception.UserNotFoundException;
import com.internship.auth_service.security.JwtUtil;
import com.internship.auth_service.service.AuthService;
import com.internship.auth_service.service.TokenManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenManagementServiceImpl implements TokenManagementService {

    private static final String INVALID_LOGIN_OR_PASSWORD = "Invalid login or password";
    private static final String AUTHENTICATION_FAILED = "Authentication failed: ";
    private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    private static final String TOKEN_REFRESH_FAILED = "Token refresh failed: ";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String TOKEN_VALIDATION_ERROR = "Token validation error";
    private static final String TOKEN_IS_VALID = "Token is valid";

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getLogin(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtil.generateAccessToken(loginRequest.getLogin());
            String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getLogin());

            return new TokenResponse(accessToken, refreshToken, jwtUtil.getAccessTokenExpiration());

        } catch (BadCredentialsException e) {
            throw new AuthenticationException(INVALID_LOGIN_OR_PASSWORD);
        } catch (Exception e) {
            throw new AuthenticationException(AUTHENTICATION_FAILED + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            if (!jwtUtil.validateToken(refreshTokenRequest.getRefreshToken())) {
                throw new InvalidTokenException(INVALID_REFRESH_TOKEN);
            }

            String login = jwtUtil.extractLogin(refreshTokenRequest.getRefreshToken());

            if (!authService.userExists(login)) {
                throw new UserNotFoundException(login);
            }

            String newAccessToken = jwtUtil.generateAccessToken(login);
            String newRefreshToken = jwtUtil.generateRefreshToken(login);

            return new TokenResponse(newAccessToken, newRefreshToken, jwtUtil.getAccessTokenExpiration());

        } catch (AuthServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException(TOKEN_REFRESH_FAILED + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(ValidateTokenRequest validateTokenRequest) {
        try {
            if (!jwtUtil.validateToken(validateTokenRequest.getToken())) {
                return new TokenValidationResponse(false, null, INVALID_REFRESH_TOKEN);
            }

            String login = jwtUtil.extractLogin(validateTokenRequest.getToken());

            if (!authService.userExists(login)) {
                return new TokenValidationResponse(false, null, USER_NOT_FOUND_MESSAGE);
            }

            return new TokenValidationResponse(true, login, TOKEN_IS_VALID);

        } catch (Exception e) {
            return new TokenValidationResponse(false, null, TOKEN_VALIDATION_ERROR);
        }
    }
}

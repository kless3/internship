package com.internship.auth_service.service.impl;

import com.internship.auth_service.dto.LoginRequest;
import com.internship.auth_service.dto.TokenValidationResponse;
import com.internship.auth_service.dto.RefreshTokenRequest;
import com.internship.auth_service.dto.TokenResponse;
import com.internship.auth_service.dto.ValidateTokenRequest;
import com.internship.auth_service.exception.AuthServiceException;
import com.internship.auth_service.exception.AuthenticationException;
import com.internship.auth_service.exception.InvalidTokenException;
import com.internship.auth_service.exception.UserNotFoundException;
import com.internship.auth_service.security.JwtTokenProvider;
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

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.login(),
                            loginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtTokenProvider.generateAccessToken(loginRequest.login());
            String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.login());

            return new TokenResponse(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiration());

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
            if (!jwtTokenProvider.validateToken(refreshTokenRequest.refreshToken())) {
                throw new InvalidTokenException(INVALID_REFRESH_TOKEN);
            }

            String login = jwtTokenProvider.getUsernameFromToken(refreshTokenRequest.refreshToken());

            if (!authService.userExists(login)) {
                throw new UserNotFoundException(login);
            }

            String newAccessToken = jwtTokenProvider.generateAccessToken(login);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(login);

            return new TokenResponse(newAccessToken, newRefreshToken, jwtTokenProvider.getAccessTokenExpiration());

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
            if (!jwtTokenProvider.validateToken(validateTokenRequest.token())) {
                return new TokenValidationResponse(false, null, INVALID_REFRESH_TOKEN);
            }

            String login = jwtTokenProvider.getUsernameFromToken(validateTokenRequest.token());

            if (!authService.userExists(login)) {
                return new TokenValidationResponse(false, null, USER_NOT_FOUND_MESSAGE);
            }

            return new TokenValidationResponse(true, login, TOKEN_IS_VALID);

        } catch (Exception e) {
            return new TokenValidationResponse(false, null, TOKEN_VALIDATION_ERROR);
        }
    }
}
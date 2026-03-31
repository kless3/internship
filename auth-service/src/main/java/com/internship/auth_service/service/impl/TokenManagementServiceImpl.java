package com.internship.auth_service.service.impl;

import com.internship.auth_service.dto.LoginRequest;
import com.internship.auth_service.dto.TokenValidationResponse;
import com.internship.auth_service.dto.TokenResponse;
import com.internship.auth_service.dto.ValidateTokenRequest;
import com.internship.auth_service.exception.AuthServiceException;
import com.internship.auth_service.exception.AuthenticationException;
import com.internship.auth_service.exception.InvalidTokenException;
import com.internship.auth_service.exception.UserNotFoundException;
import com.internship.auth_service.security.JwtTokenProvider;
import com.internship.auth_service.service.AuthService;
import com.internship.auth_service.service.TokenManagementService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

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
    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";
    private static final String REFRESH_COOKIE_SAME_SITE = "Strict";
    private static final String REFRESH_TOKEN_MISSING = "Refresh token is missing";

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

            return new TokenResponse(accessToken, jwtTokenProvider.getAccessTokenExpiration());

        } catch (BadCredentialsException e) {
            throw new AuthenticationException(INVALID_LOGIN_OR_PASSWORD);
        } catch (Exception e) {
            throw new AuthenticationException(AUTHENTICATION_FAILED + e.getMessage());
        }
    }

    @Override
    @Transactional
    public TokenResponse loginWithRefreshCookie(LoginRequest loginRequest, HttpServletResponse response) {
        TokenResponse tokenResponse = login(loginRequest);
        String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.login());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken));
        return tokenResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new InvalidTokenException(INVALID_REFRESH_TOKEN);
            }

            String login = jwtTokenProvider.getUsernameFromToken(refreshToken);

            if (!authService.userExists(login)) {
                throw new UserNotFoundException(login);
            }

            String newAccessToken = jwtTokenProvider.generateAccessToken(login);

            return new TokenResponse(newAccessToken, jwtTokenProvider.getAccessTokenExpiration());

        } catch (AuthServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException(TOKEN_REFRESH_FAILED + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refreshWithRotatedCookie(String refreshTokenFromCookie, HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(refreshTokenFromCookie);
        TokenResponse tokenResponse = refreshToken(refreshToken);
        String login = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String rotatedRefreshToken = jwtTokenProvider.generateRefreshToken(login);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(rotatedRefreshToken));
        return tokenResponse;
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

    @Override
    public String resolveRefreshToken(String refreshTokenFromCookie) {
        if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isBlank()) {
            return refreshTokenFromCookie;
        }

        throw new InvalidTokenException(REFRESH_TOKEN_MISSING);
    }

    @Override
    public String buildRefreshCookie(String token) {
        long maxAgeSeconds = TimeUnit.MILLISECONDS.toSeconds(jwtTokenProvider.getRefreshTokenExpiration());
        return ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path(REFRESH_COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .sameSite(REFRESH_COOKIE_SAME_SITE)
                .build()
                .toString();
    }

    @Override
    public String clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .sameSite(REFRESH_COOKIE_SAME_SITE)
                .build()
                .toString();
    }
}

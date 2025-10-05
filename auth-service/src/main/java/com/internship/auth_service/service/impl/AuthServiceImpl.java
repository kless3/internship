package com.internship.auth_service.service.impl;

import com.internship.auth_service.dto.*;
import com.internship.auth_service.exception.*;
import com.internship.auth_service.model.UserCredentials;
import com.internship.auth_service.repository.UserCredentialsRepository;
import com.internship.auth_service.service.AuthService;
import com.internship.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserCredentialsRepository userCredentialsRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

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
            throw new AuthenticationException("Invalid login or password");
        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            if (!jwtUtil.validateToken(refreshTokenRequest.getRefreshToken())) {
                throw new InvalidTokenException("Invalid refresh token");
            }

            String login = jwtUtil.extractLogin(refreshTokenRequest.getRefreshToken());

            if (!userCredentialsRepository.existsByLogin(login)) {
                throw new UserNotFoundException(login);
            }

            String newAccessToken = jwtUtil.generateAccessToken(login);
            String newRefreshToken = jwtUtil.generateRefreshToken(login);

            return new TokenResponse(newAccessToken, newRefreshToken, jwtUtil.getAccessTokenExpiration());

        } catch (AuthServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(ValidateTokenRequest validateTokenRequest) {
        try {
            if (!jwtUtil.validateToken(validateTokenRequest.getToken())) {
                return new TokenValidationResponse(false, null, "Invalid token");
            }

            String login = jwtUtil.extractLogin(validateTokenRequest.getToken());

            if (!userCredentialsRepository.existsByLogin(login)) {
                return new TokenValidationResponse(false, null, "User not found");
            }

            return new TokenValidationResponse(true, login, "Token is valid");

        } catch (Exception e) {
            return new TokenValidationResponse(false, null, "Token validation error");
        }
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        if (userCredentialsRepository.existsByLogin(registerRequest.getLogin())) {
            throw new DuplicateLoginException(registerRequest.getLogin());
        }

        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setLogin(registerRequest.getLogin());
        userCredentials.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        userCredentials.setEnabled(true);

        userCredentialsRepository.save(userCredentials);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String login) {
        return userCredentialsRepository.existsByLogin(login);
    }
}
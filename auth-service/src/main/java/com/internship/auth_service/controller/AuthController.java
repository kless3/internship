package com.internship.auth_service.controller;


import com.internship.auth_service.dto.LoginRequest;
import com.internship.auth_service.dto.TokenValidationResponse;
import com.internship.auth_service.dto.RegisterRequest;
import com.internship.auth_service.dto.RefreshTokenRequest;
import com.internship.auth_service.dto.TokenResponse;
import com.internship.auth_service.dto.ValidateTokenRequest;
import com.internship.auth_service.service.AuthService;
import com.internship.auth_service.service.TokenManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String USER_REGISTRATION_SUCCESS = "User registered successfully";
    private static final String SERVICE_HEALTHY = "Auth Service is healthy";

    private final AuthService authService;
    private final TokenManagementService tokenManagementService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = tokenManagementService.login(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        TokenResponse tokenResponse = tokenManagementService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@Valid @RequestBody ValidateTokenRequest validateTokenRequest) {
        TokenValidationResponse validationResponse = tokenManagementService.validateToken(validateTokenRequest);
        return ResponseEntity.ok(validationResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(Map.of("message", USER_REGISTRATION_SUCCESS));
    }

    @GetMapping("/check/{login}")
    public ResponseEntity<Map<String, Boolean>> checkUserExists(@PathVariable String login) {
        boolean exists = authService.userExists(login);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", SERVICE_HEALTHY));
    }
}
package com.internship.auth_service.controller;

import com.internship.auth_service.dto.*;
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
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @GetMapping("/check/{login}")
    public ResponseEntity<Map<String, Boolean>> checkUserExists(@PathVariable String login) {
        boolean exists = authService.userExists(login);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Auth Service is healthy"));
    }
}
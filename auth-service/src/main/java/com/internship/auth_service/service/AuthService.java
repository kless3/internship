package com.internship.auth_service.service;

import com.internship.auth_service.dto.RegisterRequest;

public interface AuthService {

    void registerUser(RegisterRequest registerRequest);
    boolean userExists(String login);
}
package com.internship.auth_service.service.impl;

import com.internship.auth_service.client.UserServiceClient;
import com.internship.auth_service.dto.RegisterRequest;
import com.internship.auth_service.exception.DuplicateLoginException;
import com.internship.auth_service.model.UserCredentials;
import com.internship.auth_service.repository.UserCredentialsRepository;
import com.internship.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;

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

        UserCredentials savedUser = userCredentialsRepository.save(userCredentials);

        Map<String, Object> userProfileData = Map.of(
                "name", registerRequest.getName(),
                "surname", registerRequest.getSurname(),
                "birthDate", registerRequest.getBirthDate(),
                "email", registerRequest.getEmail()
        );

        boolean userProfileCreated = userServiceClient.createUserProfile(userProfileData);

        if (!userProfileCreated) {
            userCredentialsRepository.delete(savedUser);
            throw new RuntimeException("Failed to create user profile");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String login) {
        return userCredentialsRepository.existsByLogin(login);
    }
}
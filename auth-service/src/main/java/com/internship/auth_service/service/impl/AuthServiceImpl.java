package com.internship.auth_service.service.impl;

import com.internship.auth_service.client.UserServiceClient;
import com.internship.auth_service.dto.RegisterRequest;
import com.internship.auth_service.dto.UserServiceRequest;
import com.internship.auth_service.event.UserCreationRollbackEvent;
import com.internship.auth_service.exception.DuplicateLoginException;
import com.internship.auth_service.exception.UserServiceException;
import com.internship.auth_service.model.UserCredentials;
import com.internship.auth_service.repository.UserCredentialsRepository;
import com.internship.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String USER_SERVICE_ERROR = "Failed to create user profile: ";
    private static final String AUTH_SERVICE_ERROR = "Failed to create user credentials: ";
    private static final String FAILED_ROLLBACK = "Failed rollback: ";

    private final UserServiceClient userServiceClient;
    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        String sagaId = UUID.randomUUID().toString();

        if (userCredentialsRepository.existsByLogin(registerRequest.login())) {
            throw new DuplicateLoginException(registerRequest.login());
        }

        try {
            UserServiceRequest userRequestDTO = new UserServiceRequest(
                    registerRequest.name(),
                    registerRequest.surname(),
                    registerRequest.birthDate(),
                    registerRequest.email()
            );

            userServiceClient.registerUser(userRequestDTO);

            UserCredentials userCredentials = new UserCredentials();
            userCredentials.setLogin(registerRequest.login());
            userCredentials.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
            userCredentials.setEnabled(true);

            UserCredentials savedUser = userCredentialsRepository.save(userCredentials);

        } catch (Exception e) {
            initiateUserServiceRollback(sagaId, registerRequest.email(),
                    AUTH_SERVICE_ERROR + e.getMessage());
            throw new UserServiceException(USER_SERVICE_ERROR + e.getMessage());
        }
    }

    private void initiateUserServiceRollback(String sagaId, String email, String reason) {
        try {
            UserCreationRollbackEvent rollbackEvent = UserCreationRollbackEvent.of(sagaId, email, reason);
            userServiceClient.deleteUserByEmail(email);

        } catch (Exception rollbackException) {
            throw new UserServiceException(FAILED_ROLLBACK + rollbackException.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String login) {
        return userCredentialsRepository.existsByLogin(login);
    }
}
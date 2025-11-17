package com.internship.auth_service.service.impl;

import com.internship.auth_service.model.UserCredentials;
import com.internship.auth_service.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final String USER_NOT_FOUND_WITH_LOGIN = "User not found with login: ";
    private static final String USER_ACCOUNT_DISABLED = "User account is disabled: ";


    private final UserCredentialsRepository userCredentialsRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        UserCredentials userCredentials = userCredentialsRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_LOGIN + login));

        if (!userCredentials.isEnabled()) {
            throw new UsernameNotFoundException(USER_ACCOUNT_DISABLED + login);
        }

        return User.builder()
                .username(userCredentials.getLogin())
                .password(userCredentials.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .disabled(!userCredentials.isEnabled())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}
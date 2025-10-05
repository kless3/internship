package com.internship.auth_service.repository;

import com.internship.auth_service.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {

    Optional<UserCredentials> findByLogin(String login);
    boolean existsByLogin(String login);
    long countByLogin(String login);
}
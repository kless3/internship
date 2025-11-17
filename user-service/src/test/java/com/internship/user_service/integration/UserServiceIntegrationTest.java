package com.internship.user_service.integration;

import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.exception.DuplicateResourceException;
import com.internship.user_service.exception.ResourceNotFoundException;
import com.internship.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Integration Tests for UserServiceImpl")
class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    private UserRequestDTO testUserRequest;

    @BeforeEach
    void setUp() {
        testUserRequest = new UserRequestDTO(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe@example.com"
        );
    }

    @Test
    @DisplayName("Create user - should create user successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createUser_ShouldCreateUserSuccessfully() {
        UserResponseDTO createdUser = userService.createUser(testUserRequest);

        assertNotNull(createdUser.id());
        assertEquals(testUserRequest.name(), createdUser.name());
        assertEquals(testUserRequest.surname(), createdUser.surname());
        assertEquals(testUserRequest.email(), createdUser.email());
    }

    @Test
    @DisplayName("Create user with existing email - should throw exception")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createUser_WithExistingEmail_ShouldThrowException() {
        userService.createUser(testUserRequest);

        assertThrows(DuplicateResourceException.class, () -> {
            userService.createUser(testUserRequest);
        });
    }

    @Test
    @DisplayName("Get user by ID - should return user")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getUserById_ShouldReturnUser() {
        UserResponseDTO createdUser = userService.createUser(testUserRequest);

        UserResponseDTO foundUser = userService.getUserById(createdUser.id());

        assertNotNull(foundUser);
        assertEquals(createdUser.id(), foundUser.id());
        assertEquals(createdUser.email(), foundUser.email());
    }

    @Test
    @DisplayName("Get user by ID with non-existing ID - should throw exception")
    void getUserById_WithNonExistingId_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
    }

    @Test
    @DisplayName("Get user by email - should return user")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getUserByEmail_ShouldReturnUser() {
        UserResponseDTO createdUser = userService.createUser(testUserRequest);

        UserResponseDTO foundUser = userService.getUserByEmail(createdUser.email());

        assertNotNull(foundUser);
        assertEquals(createdUser.id(), foundUser.id());
        assertEquals(createdUser.email(), foundUser.email());
    }

    @Test
    @DisplayName("Update user - should update user successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUser_ShouldUpdateUserSuccessfully() {
        UserResponseDTO createdUser = userService.createUser(testUserRequest);

        UserRequestDTO updateRequest = new UserRequestDTO(
                "Jane",
                "Smith",
                LocalDate.of(1991, 2, 1),
                "jane.smith@example.com"
        );

        UserResponseDTO updatedUser = userService.updateUser(createdUser.id(), updateRequest);

        assertEquals(createdUser.id(), updatedUser.id());
        assertEquals("Jane", updatedUser.name());
        assertEquals("Smith", updatedUser.surname());
        assertEquals("jane.smith@example.com", updatedUser.email());
    }

    @Test
    @DisplayName("Get all users - should return all users")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllUsers_ShouldReturnAllUsers() {
        userService.createUser(testUserRequest);

        UserRequestDTO anotherUser = new UserRequestDTO(
                "Alice",
                "Johnson",
                LocalDate.of(1992, 3, 1),
                "alice@example.com"
        );
        userService.createUser(anotherUser);

        List<UserResponseDTO> users = userService.getAllUsers();

        assertTrue(users.size() >= 2);
    }

    @Test
    @DisplayName("Delete user - should delete user successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteUser_ShouldDeleteUserSuccessfully() {
        UserResponseDTO createdUser = userService.createUser(testUserRequest);

        userService.deleteUser(createdUser.id());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(createdUser.id());
        });
        assertFalse(userService.userExists(createdUser.id()));
    }
}
package com.internship.user_service.controller;

import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.exception.DuplicateResourceException;
import com.internship.user_service.exception.ResourceNotFoundException;
import com.internship.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private static final String USER_NOT_FOUND_BY_ID = "User not found with id: ";
    private static final String USER_NOT_FOUND_BY_EMAIL = "User not found with email: ";
    private static final String USER_EMAIL_ALREADY_EXISTS = "User with email %s already exists";
    private static final String EMAIL_ALREADY_EXISTS = "Email %s already exists";

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_BY_ID + id);
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        UserResponseDTO user = userService.getUserByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_BY_EMAIL + email);
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/byIds")
    public ResponseEntity<List<UserResponseDTO>> getUsersByIds(@RequestParam List<Long> ids) {
        List<UserResponseDTO> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable Long id) {
        boolean exists = userService.userExists(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/email/{email}/exists")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequest) {
        try {
            UserResponseDTO createdUser = userService.createUser(userRequest);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (DuplicateResourceException e) {
            throw new DuplicateResourceException(String.format(USER_EMAIL_ALREADY_EXISTS, userRequest.getEmail()));
        }
    }

    @PutMapping("/{id}/upd")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDTO updateRequest) {
        try {
            UserResponseDTO updatedUser = userService.updateUser(id, updateRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_BY_ID + id);
        } catch (DuplicateResourceException e) {
            throw new DuplicateResourceException(String.format(EMAIL_ALREADY_EXISTS, updateRequest.getEmail()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_BY_ID + id);
        }
    }
}
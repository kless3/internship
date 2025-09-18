package com.internship.user_service.service;

import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO getUserByEmail(String email);

    List<UserResponseDTO> getUsersByIds(List<Long> ids);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO);

    void deleteUser(Long id);

    boolean userExists(Long id);

    boolean emailExists(String email);
}
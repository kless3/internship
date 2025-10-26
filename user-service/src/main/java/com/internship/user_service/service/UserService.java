package com.internship.user_service.service;

import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.model.User;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO getUserByEmail(String email);

    List<UserResponseDTO> getUsersByIds(List<Long> ids);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO);

    User getUserEntityById(Long id);

    void deleteUser(Long id);

    void deleteUserByEmail(String email);

    boolean userExists(Long id);

    boolean emailExists(String email);
}
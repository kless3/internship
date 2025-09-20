package com.internship.user_service.service.impl;

import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.mapper.UserMapper;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("John");
        user.setSurname("Doe");

        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setName("John");
        userRequestDTO.setSurname("Doe");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setName("John");
        userResponseDTO.setSurname("Doe");
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(false);
        when(userMapper.toEntity(userRequestDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.createUser(userRequestDTO);

        assertNotNull(result);
        assertEquals(userResponseDTO.getId(), result.getId());
        assertEquals(userResponseDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(userRequestDTO));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(userResponseDTO.getId(), result.getId());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                userService.getUserById(1L));
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(userResponseDTO.getEmail(), result.getEmail());
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.updateUser(1L, userRequestDTO);

        assertNotNull(result);
        verify(userMapper, times(1)).updateEntityFromDTO(userRequestDTO, user);
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateUser_EmailExists_ThrowsException() {
        UserRequestDTO updateRequest = new UserRequestDTO();
        updateRequest.setEmail("different@example.com"); // Different email
        updateRequest.setName("John");
        updateRequest.setSurname("Doe");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("different@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(1L, updateRequest));
    }

    @Test
    void updateUser_SameEmail_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.updateUser(1L, userRequestDTO);

        assertNotNull(result);
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
                userService.deleteUser(1L));
    }

    @Test
    void getUsersByIds_Success() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<User> users = Arrays.asList(user, new User());
        when(userRepository.findByIdIn(ids)).thenReturn(users);
        when(userMapper.toDTO(any(User.class))).thenReturn(userResponseDTO);

        List<UserResponseDTO> result = userService.getUsersByIds(ids);

        assertEquals(2, result.size());
    }

    @Test
    void getAllUsers_Success() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDTO(any(User.class))).thenReturn(userResponseDTO);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
    }

    @Test
    void userExists_ReturnsTrue() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertTrue(userService.userExists(1L));
    }

    @Test
    void userExists_ReturnsFalse() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertFalse(userService.userExists(1L));
    }

    @Test
    void emailExists_ReturnsTrue() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertTrue(userService.emailExists("test@example.com"));
    }
}
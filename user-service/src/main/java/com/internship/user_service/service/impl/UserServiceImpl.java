package com.internship.user_service.service.impl;

import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.exception.DuplicateResourceException;
import com.internship.user_service.exception.ResourceNotFoundException;
import com.internship.user_service.mapper.UserMapper;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.UserRepository;
import com.internship.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@CacheConfig(cacheNames = "users")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
    private static final String USER_NOT_FOUND_WITH_ID_ENTITY = "User not found with id ";
    private static final String USER_ALREADY_EXISTS_WITH_EMAIL = "User with email %s already exists";
    private static final String EMAIL_ALREADY_EXISTS = "Email %s already exists";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {

        if (userRepository.existsByEmail(userRequestDTO.email())) {
            throw new DuplicateResourceException(String.format(USER_ALREADY_EXISTS_WITH_EMAIL, userRequestDTO.email()));
        }

        User user = userMapper.toEntity(userRequestDTO);
        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id", unless = "#result == null")
    public UserResponseDTO getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'email:' + #email", unless = "#result == null")
    public UserResponseDTO getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + email));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByIds(List<Long> ids) {

        List<User> users = userRepository.findByIdIn(ids);
        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID_ENTITY + id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all'", unless = "#result.isEmpty()")
    public List<UserResponseDTO> getAllUsers() {

        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    @CachePut(key = "#id")
    @CacheEvict(key = "'all'")
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));

        if (!user.getEmail().equals(userRequestDTO.email()) &&
                userRepository.existsByEmail(userRequestDTO.email())) {
            throw new DuplicateResourceException(String.format(EMAIL_ALREADY_EXISTS, userRequestDTO.email()));
        }

        userMapper.updateEntityFromDTO(userRequestDTO, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + email));

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
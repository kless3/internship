package com.internship.user_service.service.impl;

import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.mapper.UserMapper;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.UserRepository;
import com.internship.user_service.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {

        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("User with email " + userRequestDTO.getEmail() + " already exists");
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
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#email", unless = "#result == null")
    public UserResponseDTO getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

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
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
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
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(userRequestDTO.getEmail()) &&
                userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Email " + userRequestDTO.getEmail() + " already exists");
        }

        userMapper.updateEntityFromDTO(userRequestDTO, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(key = "{'all', #id}")
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);

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

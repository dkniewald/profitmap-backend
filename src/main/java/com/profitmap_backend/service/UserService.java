package com.profitmap_backend.service;

import com.profitmap_backend.dto.UserDto;
import com.profitmap_backend.model.User;
import com.profitmap_backend.model.UserStatus;
import com.profitmap_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Updates the status of a user by ID.
     * 
     * @param userId The ID of the user to update
     * @param status The new status to set
     * @return The updated UserDto
     * @throws IllegalArgumentException if user is not found
     */
    @Transactional
    public UserDto updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        UserStatus oldStatus = user.getStatus();
        user.setStatus(status);
        userRepository.save(user);
        
        log.info("Updated user {} status from {} to {}", user.getEmail(), oldStatus, status);
        
        return toDto(user);
    }
    
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return toDto(user);
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCountry(),
                user.getDateOfBirth(),
                user.getPhoneNumber(),
                user.getCompany() != null ? user.getCompany().getId() : null,
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRoles()
        );
    }
}


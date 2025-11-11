package com.profitmap_backend.service;

import com.profitmap_backend.dto.UserDto;
import com.profitmap_backend.model.User;
import com.profitmap_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRoles()
        );
    }
}


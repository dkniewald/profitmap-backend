package com.profitmap_backend.dto;

import com.profitmap_backend.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String country;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private Long companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<UserRole> roles;
}
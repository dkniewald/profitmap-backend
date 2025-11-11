package com.profitmap_backend.service;

import com.profitmap_backend.dto.UserDto;
import com.profitmap_backend.model.Company;
import com.profitmap_backend.model.User;
import com.profitmap_backend.model.UserRole;
import com.profitmap_backend.repository.UserRepository;
import com.profitmap_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public ResponseEntity<?> register(Object registerRequestObj) {
        // Cast to RegisterRequest
        com.profitmap_backend.controller.AuthController.RegisterRequest registerRequest =
            (com.profitmap_backend.controller.AuthController.RegisterRequest) registerRequestObj;
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already in use");
        }
        LocalDate dob = null;
        if (registerRequest.getDateOfBirth() != null) {
            try {
                dob = LocalDate.parse(registerRequest.getDateOfBirth());
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body("Invalid date_of_birth format. Use YYYY-MM-DD.");
            }
        }
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .country(registerRequest.getCountry())
                .dateOfBirth(dob)
                .phoneNumber(registerRequest.getPhoneNumber())
                .build();
        user.getRoles().add(UserRole.USER);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        UserDto userDto = new UserDto(
            user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
            user.getCountry(), user.getDateOfBirth(), user.getPhoneNumber(),
            user.getCompany() != null ? user.getCompany().getId() : null,
            user.getCreatedAt(), user.getUpdatedAt(),
            user.getRoles()
        );
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<?> login(Object loginRequestObj) {
        // Cast to LoginRequest
        com.profitmap_backend.controller.AuthController.LoginRequest loginRequest =
            (com.profitmap_backend.controller.AuthController.LoginRequest) loginRequestObj;
        User user = userRepository.findByEmail(loginRequest.getUsernameOrEmail())
                .or(() -> userRepository.findByUsername(loginRequest.getUsernameOrEmail()))
                .orElse(null);
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        Company c = user.getCompany();

        if (c != null){
            if (!c.getIsActive()){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Your account is currently not active, please contact ProfitMap team for support.");
            }

            if (c.getIsDemo() && c.getDemoExpiration() != null && c.getDemoExpiration().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Demo period expired, please contact ProfitMap team for support.");
            }
        }


        String token = jwtUtil.generateToken(user);
        UserDto userDto = new UserDto(
            user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
            user.getCountry(), user.getDateOfBirth(), user.getPhoneNumber(),
            user.getCompany() != null ? user.getCompany().getId() : null,
            user.getCreatedAt(), user.getUpdatedAt(),
            user.getRoles()
        );
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userDto);
        return ResponseEntity.ok(response);
    }
} 
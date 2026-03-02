package com.healthapp.backend.dto;

import com.healthapp.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Login response DTO containing JWT token and user information.
 * Token should be included in Authorization header as "Bearer {token}" for authenticated requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token; // JWT token (24-hour expiration)
    private String type = "Bearer"; // Token type for Authorization header
    private UUID userId; // User's unique identifier
    private String email;
    private Role role; // PATIENT, DOCTOR, or ADMIN
    private String message;

    /**
     * Constructor used by AuthService for successful login response.
     */
    public LoginResponse(String token, UUID userId, String email, Role role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.message = "Login successful";
    }
}
package com.healthapp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO containing user credentials.
 * Email must be verified before login is allowed (checked in AuthService).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email; // User's email address (username)

    @NotBlank(message = "Password is required")
    private String password; // Plain text password (hashed during authentication)
}
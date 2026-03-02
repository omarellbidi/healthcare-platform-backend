package com.healthapp.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for user management in admin panel.
 */
@Data
public class AdminUserDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean verified;
    private Boolean suspended;
    private String specialization; // For doctors
    private LocalDateTime createdAt;
}

package com.healthapp.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for doctor approval in admin panel.
 */
@Data
public class DoctorApprovalDTO {
    private UUID id;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private Integer experience;
    private String education;
    private Boolean approved;
    private String rejectionReason;
    private LocalDateTime createdAt;
}

package com.healthapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Review responses.
 *
 * Contains review details along with patient and doctor information
 * for display on doctor profile pages and review lists.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private UUID id;

    private UUID appointmentId;

    // Patient info
    private UUID patientId;
    private String patientName;

    // Doctor info
    private UUID doctorId;
    private String doctorName;

    // Review content
    private Integer rating; // 1-5 stars
    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.healthapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Public-facing doctor profile DTO for search results and public view.
 *
 * Contains only information that should be visible to patients:
 * - Basic information (name, specialization)
 * - Professional details (experience, education)
 * - Rating and review count
 * - Languages and clinic location
 *
 * Does NOT include:
 * - Contact information (phone) - only visible after booking
 * - License number - internal information
 * - Profile photo - optional, not needed for search
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPublicProfileDTO {

    private UUID id;

    private String firstName;

    private String lastName;

    private String specialization;

    private Integer experience; // Years of experience

    private String education; // Degree and university

    private String bio; // Short bio

    private List<String> languages; // Languages spoken

    private String clinicAddress;

    private Double averageRating; // Average rating (1-5 stars)

    private Integer reviewCount; // Total number of reviews

    private String profilePhotoBase64; // Base64-encoded profile photo
}

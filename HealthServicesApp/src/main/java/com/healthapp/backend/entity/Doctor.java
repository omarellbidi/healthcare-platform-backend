package com.healthapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Doctor profile entity storing professional credentials and practice information.
 * Requires admin approval before being active on the platform (approved field must be true).
 */
@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // Reference to authentication user entity

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String gender; // Male, Female, or Other

    @Column(columnDefinition = "TEXT")
    private String profilePhotoBase64; // Base64-encoded image stored directly in database

    @Column(nullable = false, unique = true)
    private String licenseNumber; // Medical license number - must be unique for verification

    @Column(nullable = false)
    private String specialization; // E.g., Cardiology, Dermatology, General Practice

    private Integer experience; // Years of professional medical experience

    @Column(columnDefinition = "TEXT")
    private String education; // Degrees, certifications, medical school attended

    @Column(length = 500)
    private String bio; // Professional biography (max 500 characters)

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> languages; // Languages spoken - stored as JSON array

    @Column(columnDefinition = "TEXT")
    private String clinicAddress; // Physical address of practice location

    @Column(nullable = false)
    private Boolean approved = false; // Admin approval status - doctor cannot practice until approved

    @Column(columnDefinition = "TEXT")
    private String rejectionReason; // Reason for rejection if admin rejects doctor application

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // Timestamp when doctor profile was created

    private Double averageRating; // Average rating from reviews (1-5 stars)

    private Integer reviewCount; // Total number of reviews received
}
package com.healthapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Patient profile entity storing personal and medical information.
 * Associated with a User entity in a one-to-one relationship for authentication purposes.
 */
@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

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
    private LocalDate dob; // Date of birth for age calculation and verification

    @Column(nullable = false)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private String gender; // Male, Female, or Other

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> medicalHistory; // Medical questionnaire (includes blood type, allergies, medications, etc.)

    @Column(columnDefinition = "TEXT")
    private String profilePhotoBase64; // Base64-encoded image stored directly in database

    @Column(columnDefinition = "TEXT")
    private String insuranceInfo; // Optional insurance provider and policy details
}
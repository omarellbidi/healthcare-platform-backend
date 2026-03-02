package com.healthapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Review entity representing patient feedback for completed appointments.
 *
 * Business Rules:
 * - Only patients can submit reviews
 * - One review per appointment (prevents duplicate reviews)
 * - Only COMPLETED appointments can be reviewed
 * - Rating must be between 1-5 stars
 * - Review updates doctor's average rating automatically
 *
 * Relationships:
 * - Belongs to one Appointment (one-to-one)
 * - References Patient and Doctor for convenience
 */
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_doctor", columnList = "doctor_id"),
        @Index(name = "idx_review_appointment", columnList = "appointment_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(length = 1000)
    private String comment; // Optional review comment

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

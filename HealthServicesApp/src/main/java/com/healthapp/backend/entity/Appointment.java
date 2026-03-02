package com.healthapp.backend.entity;

import com.healthapp.backend.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Appointment entity representing a scheduled medical appointment between a patient and doctor.
 *
 * Business Rules:
 * - Patient books appointment by selecting available time slot
 * - Doctor must confirm appointment (PENDING → CONFIRMED)
 * - Cancellation requires 24-hour advance notice
 * - Maximum 1 reschedule allowed per appointment
 * - Only COMPLETED appointments can receive reviews
 *
 * Status Flow: PENDING → CONFIRMED → COMPLETED (or CANCELLED at any stage)
 */
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_doctor_date", columnList = "doctor_id, appointment_date"),
        @Index(name = "idx_appointment_patient_status", columnList = "patient_id, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(length = 500)
    private String reason; // Reason for appointment (e.g., "Annual checkup", "Follow-up")

    @Column(length = 1000)
    private String notes; // Doctor's notes after appointment

    @Column(name = "reschedule_count", nullable = false)
    private Integer rescheduleCount = 0; // Track number of reschedules (max 1)

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

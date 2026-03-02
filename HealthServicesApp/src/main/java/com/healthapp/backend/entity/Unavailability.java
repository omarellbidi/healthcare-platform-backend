package com.healthapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Unavailability entity storing doctor's unavailable periods.
 * Used for vacations, sick days, conferences, or any dates doctor is not available.
 */
@Entity
@Table(name = "unavailability")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate startDate; // First day unavailable

    @Column(nullable = false)
    private LocalDate endDate; // Last day unavailable (inclusive)

    private String reason; // e.g., "Vacation", "Conference", "Sick Leave"
}

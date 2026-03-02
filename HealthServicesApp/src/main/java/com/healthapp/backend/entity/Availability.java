package com.healthapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Availability entity storing doctor's weekly working schedule.
 * Doctor sets their working days, hours, breaks, and slot duration.
 * This applies to all weeks until doctor updates it.
 */
@Entity
@Table(name = "availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek; // MONDAY, TUESDAY, WEDNESDAY, etc.

    @Column(nullable = false)
    private LocalTime startTime; // e.g., 09:00

    @Column(nullable = false)
    private LocalTime endTime; // e.g., 17:00

    private LocalTime breakStartTime; // e.g., 12:00 (optional)

    private LocalTime breakEndTime; // e.g., 13:00 (optional)

    @Column(nullable = false)
    private Integer slotDurationMinutes; // e.g., 30 minutes
}

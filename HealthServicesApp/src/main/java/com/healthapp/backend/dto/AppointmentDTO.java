package com.healthapp.backend.dto;

import com.healthapp.backend.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Data Transfer Object for Appointment responses.
 *
 * Used to return appointment details to clients, including:
 * - Appointment scheduling information (date, time)
 * - Associated patient and doctor basic info
 * - Current status and reason
 * - Doctor's notes (visible after completion)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    private UUID id;

    // Patient info
    private UUID patientId;
    private String patientName;

    // Doctor info
    private UUID doctorId;
    private String doctorName;
    private String doctorSpecialization;

    // Appointment details
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;
    private String reason;
    private String notes; // Doctor's notes after appointment
    private Integer rescheduleCount;
}

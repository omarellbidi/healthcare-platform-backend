package com.healthapp.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for rescheduling an existing appointment.
 *
 * Patient provides new date and time slot for the appointment.
 * Business rule: Maximum 1 reschedule allowed per appointment.
 *
 * Validation ensures new date is in the future.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleAppointmentRequest {

    @NotNull(message = "New appointment date is required")
    @Future(message = "New appointment date must be in the future")
    private LocalDate newAppointmentDate;

    @NotNull(message = "New start time is required")
    private LocalTime newStartTime;

    @NotNull(message = "New end time is required")
    private LocalTime newEndTime;
}

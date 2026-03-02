package com.healthapp.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO for doctor availability data transfer.
 * Used for GET, POST, and PUT operations on availability schedules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDTO {

    private UUID id;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    @NotNull(message = "Slot duration is required")
    @Min(value = 1, message = "Slot duration must be at least 1 minute")
    private Integer slotDurationMinutes;
}

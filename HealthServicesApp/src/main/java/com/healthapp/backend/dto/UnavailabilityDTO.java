package com.healthapp.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for doctor unavailability data transfer.
 * Used for GET, POST operations on unavailable periods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnavailabilityDTO {

    private UUID id;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String reason;
}

package com.healthapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO representing an available time slot for appointments.
 * Generated from doctor's availability schedule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available; // true if slot is bookable, false if already booked
}

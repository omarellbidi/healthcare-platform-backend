package com.healthapp.backend.exception;

/**
 * Exception thrown when attempting to book an appointment that conflicts with existing bookings.
 *
 * Common scenarios:
 * - Selected time slot is already booked by another patient
 * - Doctor is unavailable at the requested time
 * - Attempting to reschedule to an occupied slot
 */
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}

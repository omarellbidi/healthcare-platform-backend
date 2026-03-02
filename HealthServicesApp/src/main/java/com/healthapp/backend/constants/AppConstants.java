package com.healthapp.backend.constants;

/**
 * Application-wide constants for business rules and configuration.
 */
public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    /**
     * Token expiry duration in hours.
     * Used for email verification tokens and password reset tokens.
     */
    public static final int TOKEN_EXPIRY_HOURS = 24;

    /**
     * Minimum hours required before appointment for cancellation.
     * Appointments can only be cancelled if more than 24 hours remain.
     */
    public static final int CANCELLATION_NOTICE_HOURS = 24;

    /**
     * Maximum number of reschedules allowed per appointment.
     */
    public static final int MAX_RESCHEDULE_COUNT = 1;

    /**
     * Hours before appointment to send first reminder email.
     */
    public static final int FIRST_REMINDER_HOURS = 24;

    /**
     * Hours before appointment to send second reminder email.
     */
    public static final int SECOND_REMINDER_HOURS = 1;
}

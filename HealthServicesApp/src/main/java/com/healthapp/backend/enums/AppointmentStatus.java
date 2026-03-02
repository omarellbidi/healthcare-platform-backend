package com.healthapp.backend.enums;

/**
 * Appointment status enumeration representing the lifecycle of an appointment.
 *
 * Status Flow:
 * PENDING → CONFIRMED → COMPLETED
 *    ↓         ↓           ↓
 *    →    CANCELLED    ←
 *
 * - PENDING: Appointment created by patient, awaiting doctor confirmation
 * - CONFIRMED: Doctor confirmed the appointment
 * - COMPLETED: Appointment finished (allows patient to leave review)
 * - CANCELLED: Appointment cancelled by patient or doctor
 */
public enum AppointmentStatus {
    PENDING,      // Appointment booked, waiting for doctor confirmation
    CONFIRMED,    // Doctor confirmed the appointment
    COMPLETED,    // Appointment finished successfully
    CANCELLED     // Appointment cancelled (by patient or doctor)
}

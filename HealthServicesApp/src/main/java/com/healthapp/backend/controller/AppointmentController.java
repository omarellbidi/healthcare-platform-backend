package com.healthapp.backend.controller;

import com.healthapp.backend.dto.ApiResponse;
import com.healthapp.backend.dto.AppointmentDTO;
import com.healthapp.backend.dto.BookAppointmentRequest;
import com.healthapp.backend.dto.RescheduleAppointmentRequest;
import com.healthapp.backend.enums.AppointmentStatus;
import com.healthapp.backend.security.UserDetailsImpl;
import com.healthapp.backend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for appointment booking and management.
 *
 * Provides endpoints for:
 * - Booking new appointments (patients)
 * - Viewing appointments (patients and doctors)
 * - Cancelling appointments (patients and doctors, 24h notice required)
 * - Rescheduling appointments (patients, max 1 reschedule)
 * - Updating appointment status (doctors - confirm/complete)
 *
 * All endpoints require JWT authentication and appropriate role-based access.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment booking and management")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Patient books a new appointment with a doctor.
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Book appointment", description = "Patient books a new appointment with a doctor")
    public ResponseEntity<AppointmentDTO> bookAppointment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody BookAppointmentRequest request) {

        AppointmentDTO appointment = appointmentService.bookAppointment(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    /**
     * Get all appointments for current user (patient or doctor).
     */
    @GetMapping
    @Operation(summary = "Get user's appointments", description = "Get all appointments for current user (patient or doctor)")
    public ResponseEntity<List<AppointmentDTO>> getAppointments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) AppointmentStatus status) {

        List<AppointmentDTO> appointments;

        // Check user role and get appropriate appointments
        if (userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PATIENT"))) {
            appointments = appointmentService.getPatientAppointments(userDetails.getId(), status);
        } else {
            appointments = appointmentService.getDoctorAppointments(userDetails.getId(), status);
        }

        return ResponseEntity.ok(appointments);
    }

    /**
     * Get details of a specific appointment.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get appointment details", description = "Get details of a specific appointment")
    public ResponseEntity<AppointmentDTO> getAppointmentById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {

        AppointmentDTO appointment = appointmentService.getAppointmentById(id, userDetails.getId());
        return ResponseEntity.ok(appointment);
    }

    /**
     * Cancel appointment (requires 24h advance notice).
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel appointment", description = "Cancel appointment (requires 24h advance notice)")
    public ResponseEntity<ApiResponse> cancelAppointment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {

        appointmentService.cancelAppointment(id, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Appointment cancelled successfully"));
    }

    /**
     * Reschedule appointment to new date/time (max 1 reschedule).
     */
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Reschedule appointment", description = "Reschedule appointment to new date/time (max 1 reschedule)")
    public ResponseEntity<AppointmentDTO> rescheduleAppointment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {

        AppointmentDTO appointment = appointmentService.rescheduleAppointment(id, userDetails.getId(), request);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Doctor confirms or completes appointment.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update appointment status", description = "Doctor confirms or completes appointment")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @RequestParam AppointmentStatus status) {

        AppointmentDTO appointment = appointmentService.updateStatus(id, userDetails.getId(), status);
        return ResponseEntity.ok(appointment);
    }
}

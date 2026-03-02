package com.healthapp.backend.service;

import com.healthapp.backend.constants.AppConstants;
import com.healthapp.backend.dto.AppointmentDTO;
import com.healthapp.backend.dto.BookAppointmentRequest;
import com.healthapp.backend.dto.RescheduleAppointmentRequest;
import com.healthapp.backend.entity.Appointment;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.Patient;
import com.healthapp.backend.enums.AppointmentStatus;
import com.healthapp.backend.exception.AppointmentConflictException;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.repository.AppointmentRepository;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling appointment booking, management, and lifecycle operations.
 *
 * Core Functionality:
 * - Book appointments with double-booking prevention
 * - Cancel appointments with 24-hour advance notice rule
 * - Reschedule appointments (max 1 reschedule per appointment)
 * - Update appointment status (doctor confirmation, completion)
 * - Retrieve appointments filtered by patient/doctor/status
 *
 * Business Rules Enforced:
 * - Slot must be available (not already booked)
 * - Cancellation requires 24-hour notice
 * - Maximum 1 reschedule per appointment
 * - Only doctors can confirm/complete appointments
 * - Email notifications sent for all status changes
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;

    /**
     * Book a new appointment for a patient.
     * Validates slot availability and prevents double-booking.
     */
    @Transactional
    public AppointmentDTO bookAppointment(UUID patientUserId, BookAppointmentRequest request) {
        // Get patient
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        // Get doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Check if slot is already booked (prevent double-booking)
        boolean isSlotTaken = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
                request.getDoctorId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
        );

        if (isSlotTaken) {
            throw new AppointmentConflictException("This time slot is already booked");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setReason(request.getReason());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setRescheduleCount(0);

        Appointment saved = appointmentRepository.save(appointment);

        // Send confirmation email to patient
        emailService.sendAppointmentConfirmation(
                patient.getUser().getEmail(),
                patient.getFirstName(),
                doctor.getFirstName() + " " + doctor.getLastName(),
                saved.getAppointmentDate(),
                saved.getStartTime()
        );

        return mapToDTO(saved);
    }

    /**
     * Cancel an appointment.
     * Requires 24-hour advance notice from appointment time.
     */
    @Transactional
    public void cancelAppointment(UUID appointmentId, UUID userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Verify user is patient or doctor
        UUID patientUserId = appointment.getPatient().getUser().getId();
        UUID doctorUserId = appointment.getDoctor().getUser().getId();

        if (!userId.equals(patientUserId) && !userId.equals(doctorUserId)) {
            throw new ResourceNotFoundException("Unauthorized to cancel this appointment");
        }

        // Check if already cancelled or completed
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentConflictException("Appointment is already cancelled");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppointmentConflictException("Cannot cancel a completed appointment");
        }

        // Check 24-hour advance notice rule
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getStartTime()
        );
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilAppointment = java.time.Duration.between(now, appointmentDateTime).toHours();

        if (hoursUntilAppointment < AppConstants.CANCELLATION_NOTICE_HOURS) {
            throw new AppointmentConflictException("Cancellation requires " + AppConstants.CANCELLATION_NOTICE_HOURS + "-hour advance notice");
        }

        // Cancel appointment
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // Send cancellation email
        emailService.sendAppointmentCancellation(
                appointment.getPatient().getUser().getEmail(),
                appointment.getPatient().getFirstName(),
                appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName(),
                appointment.getAppointmentDate(),
                appointment.getStartTime()
        );
    }

    /**
     * Reschedule an appointment to a new date/time.
     * Maximum 1 reschedule allowed per appointment.
     */
    @Transactional
    public AppointmentDTO rescheduleAppointment(
            UUID appointmentId,
            UUID patientUserId,
            RescheduleAppointmentRequest request
    ) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Verify patient owns this appointment
        if (!appointment.getPatient().getUser().getId().equals(patientUserId)) {
            throw new ResourceNotFoundException("Unauthorized to reschedule this appointment");
        }

        // Check if already rescheduled once
        if (appointment.getRescheduleCount() >= AppConstants.MAX_RESCHEDULE_COUNT) {
            throw new AppointmentConflictException("Maximum " + AppConstants.MAX_RESCHEDULE_COUNT + " reschedule allowed per appointment");
        }

        // Check if new slot is available
        boolean isSlotTaken = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
                appointment.getDoctor().getId(),
                request.getNewAppointmentDate(),
                request.getNewStartTime(),
                Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
        );

        if (isSlotTaken) {
            throw new AppointmentConflictException("New time slot is already booked");
        }

        // Update appointment
        appointment.setAppointmentDate(request.getNewAppointmentDate());
        appointment.setStartTime(request.getNewStartTime());
        appointment.setEndTime(request.getNewEndTime());
        appointment.setRescheduleCount(appointment.getRescheduleCount() + 1);
        appointment.setStatus(AppointmentStatus.PENDING); // Reset to pending for doctor confirmation

        Appointment saved = appointmentRepository.save(appointment);
        return mapToDTO(saved);
    }

    /**
     * Update appointment status (doctor confirms or completes appointment).
     */
    @Transactional
    public AppointmentDTO updateStatus(UUID appointmentId, UUID doctorUserId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Verify doctor owns this appointment
        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new ResourceNotFoundException("Unauthorized to update this appointment");
        }

        // Validate status transition
        if (newStatus == AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppointmentConflictException("Can only confirm pending appointments");
        }

        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);
        return mapToDTO(saved);
    }

    /**
     * Get all appointments for a patient, optionally filtered by status.
     */
    public List<AppointmentDTO> getPatientAppointments(UUID patientUserId, AppointmentStatus status) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        List<Appointment> appointments = status != null
                ? appointmentRepository.findByPatientIdAndStatusOrderByAppointmentDateDesc(patient.getId(), status)
                : appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patient.getId());

        return appointments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Get all appointments for a doctor, optionally filtered by status.
     */
    public List<AppointmentDTO> getDoctorAppointments(UUID doctorUserId, AppointmentStatus status) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        List<Appointment> appointments = status != null
                ? appointmentRepository.findByDoctorIdAndStatusOrderByAppointmentDateAsc(doctor.getId(), status)
                : appointmentRepository.findByDoctorIdOrderByAppointmentDateAscStartTimeAsc(doctor.getId());

        return appointments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Get single appointment details by ID.
     */
    public AppointmentDTO getAppointmentById(UUID appointmentId, UUID userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Verify user is patient or doctor
        UUID patientUserId = appointment.getPatient().getUser().getId();
        UUID doctorUserId = appointment.getDoctor().getUser().getId();

        if (!userId.equals(patientUserId) && !userId.equals(doctorUserId)) {
            throw new ResourceNotFoundException("Unauthorized to view this appointment");
        }

        return mapToDTO(appointment);
    }

    /**
     * Map Appointment entity to DTO.
     */
    private AppointmentDTO mapToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());

        // Patient info
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());

        // Doctor info
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName());
        dto.setDoctorSpecialization(appointment.getDoctor().getSpecialization());

        // Appointment details
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setStatus(appointment.getStatus());
        dto.setReason(appointment.getReason());
        dto.setNotes(appointment.getNotes());
        dto.setRescheduleCount(appointment.getRescheduleCount());

        return dto;
    }
}

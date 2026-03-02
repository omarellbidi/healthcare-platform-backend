package com.healthapp.backend.repository;

import com.healthapp.backend.entity.Appointment;
import com.healthapp.backend.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Appointment entity providing database access methods.
 *
 * Includes custom queries for:
 * - Double-booking prevention (checking if slot is already taken)
 * - Filtering appointments by patient, doctor, status, and date range
 * - Finding upcoming appointments for reminder notifications
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // Check if doctor has appointment at specific date/time (prevent double-booking)
    boolean existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime,
            List<AppointmentStatus> statuses
    );

    // Find all appointments for a patient
    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(UUID patientId);

    // Find all appointments for a doctor
    List<Appointment> findByDoctorIdOrderByAppointmentDateAscStartTimeAsc(UUID doctorId);

    // Find appointments by patient and status
    List<Appointment> findByPatientIdAndStatusOrderByAppointmentDateDesc(
            UUID patientId,
            AppointmentStatus status
    );

    // Find appointments by doctor and status
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateAsc(
            UUID doctorId,
            AppointmentStatus status
    );

    // Find appointments by doctor on specific date
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusIn(
            UUID doctorId,
            LocalDate appointmentDate,
            List<AppointmentStatus> statuses
    );

    // Find upcoming appointments for reminder notifications (between 23-25 hours from now)
    @Query("SELECT a FROM Appointment a WHERE a.status = 'CONFIRMED' " +
            "AND FUNCTION('TIMESTAMP', a.appointmentDate, a.startTime) BETWEEN :startRange AND :endRange")
    List<Appointment> findAppointmentsForReminder(
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange
    );

    // Count appointments by status for statistics
    long countByStatus(AppointmentStatus status);
}

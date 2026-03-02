package com.healthapp.backend.service;

import com.healthapp.backend.entity.Appointment;
import com.healthapp.backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for sending scheduled appointment reminder emails.
 *
 * Runs background jobs at regular intervals to:
 * - Find appointments happening in 24 hours (send first reminder)
 * - Find appointments happening in 1 hour (send final reminder)
 * - Send reminder emails to patients
 *
 * Uses Spring's @Scheduled annotation for automatic execution.
 * Configured to run every hour to check for upcoming appointments.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    /**
     * Send 24-hour reminder emails.
     * Runs every hour and finds appointments happening in 23-25 hours from now.
     * This gives a 2-hour window to catch appointments (handles job timing variations).
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void send24HourReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start24h = now.plusHours(23); // 23 hours from now
        LocalDateTime end24h = now.plusHours(25);   // 25 hours from now (2-hour window)

        List<Appointment> upcomingAppointments = appointmentRepository.findAppointmentsForReminder(start24h, end24h);

        for (Appointment appointment : upcomingAppointments) {
            try {
                emailService.sendAppointmentReminder(
                        appointment.getPatient().getUser().getEmail(),
                        appointment.getPatient().getFirstName(),
                        appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName(),
                        appointment.getAppointmentDate(),
                        appointment.getStartTime(),
                        "24 hours"
                );
                log.info("Sent 24h reminder for appointment: {}", appointment.getId());
            } catch (Exception e) {
                log.error("Failed to send 24h reminder for appointment {}: {}", appointment.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Send 1-hour reminder emails.
     * Runs every 30 minutes and finds appointments happening in 45-75 minutes from now.
     * This gives a 30-minute window to catch appointments.
     */
    @Scheduled(cron = "0 0,30 * * * *") // Every 30 minutes (at minute 0 and 30)
    public void send1HourReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start1h = now.plusMinutes(45); // 45 minutes from now
        LocalDateTime end1h = now.plusMinutes(75);   // 75 minutes from now (30-min window)

        List<Appointment> upcomingAppointments = appointmentRepository.findAppointmentsForReminder(start1h, end1h);

        for (Appointment appointment : upcomingAppointments) {
            try {
                emailService.sendAppointmentReminder(
                        appointment.getPatient().getUser().getEmail(),
                        appointment.getPatient().getFirstName(),
                        appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName(),
                        appointment.getAppointmentDate(),
                        appointment.getStartTime(),
                        "1 hour"
                );
                log.info("Sent 1h reminder for appointment: {}", appointment.getId());
            } catch (Exception e) {
                log.error("Failed to send 1h reminder for appointment {}: {}", appointment.getId(), e.getMessage(), e);
            }
        }
    }
}

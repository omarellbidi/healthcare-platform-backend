package com.healthapp.backend.dto;

import lombok.Data;

/**
 * DTO for admin dashboard statistics.
 */
@Data
public class AdminDashboardDTO {
    // User statistics
    private Long totalUsers;
    private Long totalPatients;
    private Long totalDoctors;
    private Long totalAdmins;

    // Appointment statistics
    private Long pendingAppointments;
    private Long confirmedAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;

    // Review statistics
    private Long totalReviews;

    // Doctor approval
    private Long pendingDoctors;

    // Activity
    private Long recentActivity; // Last 24 hours
}

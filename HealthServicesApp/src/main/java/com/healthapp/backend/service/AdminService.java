package com.healthapp.backend.service;

import com.healthapp.backend.dto.AdminDashboardDTO;
import com.healthapp.backend.dto.AdminUserDTO;
import com.healthapp.backend.dto.DoctorApprovalDTO;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.AppointmentStatus;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Service for admin operations.
 * Handles dashboard statistics, user management, and doctor approvals.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    /**
     * Get dashboard statistics.
     */
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardStats() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        // User counts by role
        long totalUsers = userRepository.count();
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalAdmins = userRepository.countByRole(Role.ADMIN);

        dashboard.setTotalUsers(totalUsers);
        dashboard.setTotalPatients(totalPatients);
        dashboard.setTotalDoctors(totalDoctors);
        dashboard.setTotalAdmins(totalAdmins);

        // Appointment counts by status
        long pendingAppointments = appointmentRepository.countByStatus(AppointmentStatus.PENDING);
        long confirmedAppointments = appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED);
        long completedAppointments = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        long cancelledAppointments = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);

        dashboard.setPendingAppointments(pendingAppointments);
        dashboard.setConfirmedAppointments(confirmedAppointments);
        dashboard.setCompletedAppointments(completedAppointments);
        dashboard.setCancelledAppointments(cancelledAppointments);

        // Review count
        long totalReviews = reviewRepository.count();
        dashboard.setTotalReviews(totalReviews);

        // Pending doctors (awaiting approval)
        long pendingDoctors = doctorRepository.countByApproved(false);
        dashboard.setPendingDoctors(pendingDoctors);

        // Recent activity (last 24 hours)
        long recentActivity = auditLogService.getRecentActivityCount();
        dashboard.setRecentActivity(recentActivity);

        return dashboard;
    }

    /**
     * Get all users with pagination and search.
     */
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users;
        if (search != null && !search.isEmpty()) {
            users = userRepository.findByEmailContainingIgnoreCase(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::mapToAdminUserDTO);
    }

    /**
     * Get user details by ID.
     */
    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToAdminUserDTO(user);
    }

    /**
     * Suspend a user account.
     */
    @Transactional
    public Map<String, Object> suspendUser(UUID userId, UUID adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Cannot suspend admin users");
        }

        if (user.getSuspended()) {
            throw new IllegalStateException("User is already suspended");
        }

        user.setSuspended(true);
        userRepository.save(user);

        // Log the action
        auditLogService.log(adminId, "USER_SUSPENDED", "USER", userId,
                "User " + user.getEmail() + " suspended by admin");

        // Send notification email
        emailService.sendAccountSuspendedEmail(user.getEmail());

        return Map.of(
                "success", true,
                "message", "User account suspended successfully"
        );
    }

    /**
     * Activate a suspended user account.
     */
    @Transactional
    public Map<String, Object> activateUser(UUID userId, UUID adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getSuspended()) {
            throw new IllegalStateException("User is not suspended");
        }

        user.setSuspended(false);
        userRepository.save(user);

        // Log the action
        auditLogService.log(adminId, "USER_ACTIVATED", "USER", userId,
                "User " + user.getEmail() + " activated by admin");

        // Send notification email
        emailService.sendAccountActivatedEmail(user.getEmail());

        return Map.of(
                "success", true,
                "message", "User account activated successfully"
        );
    }

    /**
     * Get pending doctors (awaiting approval).
     */
    @Transactional(readOnly = true)
    public Page<DoctorApprovalDTO> getPendingDoctors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Doctor> doctors = doctorRepository.findByApproved(false, pageable);

        return doctors.map(this::mapToDoctorApprovalDTO);
    }

    /**
     * Approve a doctor.
     */
    @Transactional
    public Map<String, Object> approveDoctor(UUID doctorId, UUID adminId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (doctor.getApproved()) {
            throw new IllegalStateException("Doctor is already approved");
        }

        doctor.setApproved(true);
        doctor.setRejectionReason(null);
        doctorRepository.save(doctor);

        // Log the action
        auditLogService.log(adminId, "DOCTOR_APPROVED", "DOCTOR", doctorId,
                "Doctor " + doctor.getUser().getEmail() + " approved by admin");

        // Send approval email
        String doctorName = "Dr. " + doctor.getFirstName() + " " + doctor.getLastName();
        emailService.sendDoctorApprovedEmail(doctor.getUser().getEmail(), doctorName);

        return Map.of(
                "success", true,
                "message", "Doctor approved successfully"
        );
    }

    /**
     * Reject a doctor application.
     */
    @Transactional
    public Map<String, Object> rejectDoctor(UUID doctorId, UUID adminId, String reason) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (doctor.getApproved()) {
            throw new IllegalStateException("Cannot reject an approved doctor");
        }

        doctor.setRejectionReason(reason);
        doctorRepository.save(doctor);

        // Log the action
        auditLogService.log(adminId, "DOCTOR_REJECTED", "DOCTOR", doctorId,
                "Doctor " + doctor.getUser().getEmail() + " rejected. Reason: " + reason);

        // Send rejection email
        String doctorName = "Dr. " + doctor.getFirstName() + " " + doctor.getLastName();
        emailService.sendDoctorRejectedEmail(doctor.getUser().getEmail(), doctorName, reason);

        return Map.of(
                "success", true,
                "message", "Doctor application rejected"
        );
    }

    /**
     * Map User to AdminUserDTO.
     */
    private AdminUserDTO mapToAdminUserDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().toString());
        dto.setVerified(user.getVerified());
        dto.setSuspended(user.getSuspended());
        dto.setCreatedAt(user.getCreatedAt());

        // Add patient/doctor specific info if available
        if (user.getPatient() != null) {
            dto.setFirstName(user.getPatient().getFirstName());
            dto.setLastName(user.getPatient().getLastName());
        } else if (user.getDoctor() != null) {
            dto.setFirstName(user.getDoctor().getFirstName());
            dto.setLastName(user.getDoctor().getLastName());
            dto.setSpecialization(user.getDoctor().getSpecialization());
        }

        return dto;
    }

    /**
     * Map Doctor to DoctorApprovalDTO.
     */
    private DoctorApprovalDTO mapToDoctorApprovalDTO(Doctor doctor) {
        DoctorApprovalDTO dto = new DoctorApprovalDTO();
        dto.setId(doctor.getId());
        dto.setUserId(doctor.getUser().getId());
        dto.setEmail(doctor.getUser().getEmail());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setExperience(doctor.getExperience());
        dto.setEducation(doctor.getEducation());
        dto.setApproved(doctor.getApproved());
        dto.setRejectionReason(doctor.getRejectionReason());
        dto.setCreatedAt(doctor.getCreatedAt());

        return dto;
    }
}

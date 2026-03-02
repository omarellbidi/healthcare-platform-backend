package com.healthapp.backend.controller;

import com.healthapp.backend.dto.AdminDashboardDTO;
import com.healthapp.backend.dto.AdminUserDTO;
import com.healthapp.backend.dto.ApiResponse;
import com.healthapp.backend.dto.AuditLogDTO;
import com.healthapp.backend.dto.DoctorApprovalDTO;
import com.healthapp.backend.security.UserDetailsImpl;
import com.healthapp.backend.service.AdminService;
import com.healthapp.backend.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Admin panel controller for system administration.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin panel operations - requires ADMIN role")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogService auditLogService;

    /**
     * Get dashboard statistics.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics", description = "Returns system statistics including user counts, appointment counts, and activity metrics")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getDashboard() {
        AdminDashboardDTO dashboard = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    /**
     * Get all users with search and pagination.
     */
    @GetMapping("/users")
    @Operation(summary = "List all users", description = "Get paginated list of all users with optional email search")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminUserDTO> users = adminService.getAllUsers(search, page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user details by ID.
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details", description = "Get detailed information about a specific user")
    public ResponseEntity<ApiResponse<AdminUserDTO>> getUserById(@PathVariable UUID id) {
        AdminUserDTO user = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Suspend a user account.
     */
    @PutMapping("/users/{id}/suspend")
    @Operation(summary = "Suspend user account", description = "Suspend a user account. Suspended users cannot log in.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> suspendUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Map<String, Object> result = adminService.suspendUser(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Activate a suspended user account.
     */
    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Activate user account", description = "Reactivate a suspended user account")
    public ResponseEntity<ApiResponse<Map<String, Object>>> activateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Map<String, Object> result = adminService.activateUser(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get pending doctors awaiting approval.
     */
    @GetMapping("/doctors/pending")
    @Operation(summary = "Get pending doctors", description = "Get list of doctors awaiting approval")
    public ResponseEntity<Page<DoctorApprovalDTO>> getPendingDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DoctorApprovalDTO> doctors = adminService.getPendingDoctors(page, size);
        return ResponseEntity.ok(doctors);
    }

    /**
     * Approve a doctor.
     */
    @PutMapping("/doctors/{id}/approve")
    @Operation(summary = "Approve doctor", description = "Approve a doctor's application")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveDoctor(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Map<String, Object> result = adminService.approveDoctor(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Reject a doctor application.
     */
    @PutMapping("/doctors/{id}/reject")
    @Operation(summary = "Reject doctor", description = "Reject a doctor's application with a reason")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rejectDoctor(
            @PathVariable UUID id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Map<String, Object> result = adminService.rejectDoctor(id, userDetails.getId(), reason);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get audit logs with filters.
     */
    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs", description = "Get filtered audit logs with pagination")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Page<AuditLogDTO> logs = auditLogService.getAuditLogs(
                userId, action, resourceType, startDate, endDate, page, size
        );
        return ResponseEntity.ok(logs);
    }
}

package com.healthapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.backend.dto.*;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.security.JwtTokenProvider;
import com.healthapp.backend.security.UserDetailsImpl;
import com.healthapp.backend.service.AdminService;
import com.healthapp.backend.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminController.
 * Tests admin panel endpoints with proper authorization.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private AdminDashboardDTO dashboardDTO;
    private AdminUserDTO userDTO;
    private DoctorApprovalDTO doctorApprovalDTO;
    private AuditLogDTO auditLogDTO;
    private UUID userId;
    private UUID doctorId;
    private UUID adminId;
    private UserDetailsImpl adminUser;
    private UserDetailsImpl patientUser;
    private UserDetailsImpl doctorUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        // Create admin user for authentication
        adminUser = new UserDetailsImpl(
                adminId,
                "admin@test.com",
                "password",
                Role.ADMIN,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // Create patient user for negative tests
        patientUser = new UserDetailsImpl(
                UUID.randomUUID(),
                "patient@test.com",
                "password",
                Role.PATIENT,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );

        // Create doctor user for negative tests
        doctorUser = new UserDetailsImpl(
                UUID.randomUUID(),
                "doctor@test.com",
                "password",
                Role.DOCTOR,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        // Dashboard DTO
        dashboardDTO = new AdminDashboardDTO();
        dashboardDTO.setTotalUsers(100L);
        dashboardDTO.setTotalPatients(70L);
        dashboardDTO.setTotalDoctors(25L);
        dashboardDTO.setTotalAdmins(5L);
        dashboardDTO.setPendingAppointments(10L);
        dashboardDTO.setConfirmedAppointments(15L);
        dashboardDTO.setCompletedAppointments(50L);
        dashboardDTO.setCancelledAppointments(5L);
        dashboardDTO.setTotalReviews(30L);
        dashboardDTO.setPendingDoctors(3L);
        dashboardDTO.setRecentActivity(20L);

        // User DTO
        userDTO = new AdminUserDTO();
        userDTO.setId(userId);
        userDTO.setEmail("user@test.com");
        userDTO.setRole("PATIENT");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setVerified(true);
        userDTO.setSuspended(false);
        userDTO.setCreatedAt(LocalDateTime.now());

        // Doctor Approval DTO
        doctorApprovalDTO = new DoctorApprovalDTO();
        doctorApprovalDTO.setId(doctorId);
        doctorApprovalDTO.setUserId(UUID.randomUUID());
        doctorApprovalDTO.setEmail("doctor@test.com");
        doctorApprovalDTO.setFirstName("Jane");
        doctorApprovalDTO.setLastName("Smith");
        doctorApprovalDTO.setSpecialization("Cardiology");
        doctorApprovalDTO.setLicenseNumber("LIC123456");
        doctorApprovalDTO.setExperience(5);
        doctorApprovalDTO.setEducation("MD from University");
        doctorApprovalDTO.setApproved(false);
        doctorApprovalDTO.setCreatedAt(LocalDateTime.now());

        // Audit Log DTO
        auditLogDTO = new AuditLogDTO();
        auditLogDTO.setId(UUID.randomUUID());
        auditLogDTO.setUserId(userId);
        auditLogDTO.setUserEmail("admin@test.com");
        auditLogDTO.setAction("USER_SUSPENDED");
        auditLogDTO.setResourceType("USER");
        auditLogDTO.setResourceId(userId);
        auditLogDTO.setDetails("User suspended by admin");
        auditLogDTO.setIpAddress("127.0.0.1");
        auditLogDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetDashboard_AsAdmin_ReturnsStats() throws Exception {
        // Arrange
        when(adminService.getDashboardStats()).thenReturn(dashboardDTO);

        // Act & Assert
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalPatients").value(70))
                .andExpect(jsonPath("$.data.totalDoctors").value(25))
                .andExpect(jsonPath("$.data.pendingDoctors").value(3));
    }

    @Test
    void testGetDashboard_AsPatient_Returns403() throws Exception {
        // Act & Assert - Patient should not have access
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(user(patientUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllUsers_AsAdmin_ReturnsPage() throws Exception {
        // Arrange
        Page<AdminUserDTO> usersPage = new PageImpl<>(List.of(userDTO), PageRequest.of(0, 20), 1);
        when(adminService.getAllUsers(null, 0, 20)).thenReturn(usersPage);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "20")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user@test.com"))
                .andExpect(jsonPath("$.content[0].role").value("PATIENT"));
    }

    @Test
    void testGetAllUsers_WithSearch_ReturnsFilteredPage() throws Exception {
        // Arrange
        Page<AdminUserDTO> usersPage = new PageImpl<>(List.of(userDTO), PageRequest.of(0, 20), 1);
        when(adminService.getAllUsers("user@test.com", 0, 20)).thenReturn(usersPage);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users")
                        .param("search", "user@test.com")
                        .param("page", "0")
                        .param("size", "20")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user@test.com"));
    }

    @Test
    void testGetUserById_AsAdmin_ReturnsUser() throws Exception {
        // Arrange
        when(adminService.getUserById(userId)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/{id}", userId)
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    void testSuspendUser_AsAdmin_ReturnsSuccess() throws Exception {
        // Arrange
        Map<String, Object> result = Map.of(
                "success", true,
                "message", "User account suspended successfully"
        );
        when(adminService.suspendUser(eq(userId), eq(adminId))).thenReturn(result);

        // Act & Assert
        mockMvc.perform(put("/api/admin/users/{id}/suspend", userId)
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("User account suspended successfully"));
    }

    @Test
    void testActivateUser_AsAdmin_ReturnsSuccess() throws Exception {
        // Arrange
        Map<String, Object> result = Map.of(
                "success", true,
                "message", "User account activated successfully"
        );
        when(adminService.activateUser(eq(userId), eq(adminId))).thenReturn(result);

        // Act & Assert
        mockMvc.perform(put("/api/admin/users/{id}/activate", userId)
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("User account activated successfully"));
    }

    @Test
    void testGetPendingDoctors_AsAdmin_ReturnsPage() throws Exception {
        // Arrange
        Page<DoctorApprovalDTO> doctorsPage = new PageImpl<>(
                List.of(doctorApprovalDTO),
                PageRequest.of(0, 20),
                1
        );
        when(adminService.getPendingDoctors(0, 20)).thenReturn(doctorsPage);

        // Act & Assert
        mockMvc.perform(get("/api/admin/doctors/pending")
                        .param("page", "0")
                        .param("size", "20")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("doctor@test.com"))
                .andExpect(jsonPath("$.content[0].specialization").value("Cardiology"))
                .andExpect(jsonPath("$.content[0].approved").value(false));
    }

    @Test
    void testApproveDoctor_AsAdmin_ReturnsSuccess() throws Exception {
        // Arrange
        Map<String, Object> result = Map.of(
                "success", true,
                "message", "Doctor approved successfully"
        );
        when(adminService.approveDoctor(eq(doctorId), eq(adminId))).thenReturn(result);

        // Act & Assert
        mockMvc.perform(put("/api/admin/doctors/{id}/approve", doctorId)
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Doctor approved successfully"));
    }

    @Test
    void testRejectDoctor_AsAdmin_ReturnsSuccess() throws Exception {
        // Arrange
        String rejectionReason = "Invalid license number";
        Map<String, Object> result = Map.of(
                "success", true,
                "message", "Doctor application rejected"
        );
        when(adminService.rejectDoctor(eq(doctorId), eq(adminId), eq(rejectionReason)))
                .thenReturn(result);

        // Act & Assert
        mockMvc.perform(put("/api/admin/doctors/{id}/reject", doctorId)
                        .param("reason", rejectionReason)
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Doctor application rejected"));
    }

    @Test
    void testGetAuditLogs_AsAdmin_ReturnsPage() throws Exception {
        // Arrange
        Page<AuditLogDTO> logsPage = new PageImpl<>(
                List.of(auditLogDTO),
                PageRequest.of(0, 50),
                1
        );
        when(auditLogService.getAuditLogs(null, null, null, null, null, 0, 50))
                .thenReturn(logsPage);

        // Act & Assert
        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("page", "0")
                        .param("size", "50")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("USER_SUSPENDED"))
                .andExpect(jsonPath("$.content[0].resourceType").value("USER"));
    }

    @Test
    void testGetAuditLogs_WithFilters_ReturnsFilteredPage() throws Exception {
        // Arrange
        Page<AuditLogDTO> logsPage = new PageImpl<>(
                List.of(auditLogDTO),
                PageRequest.of(0, 50),
                1
        );
        when(auditLogService.getAuditLogs(
                eq(userId),
                eq("USER_SUSPENDED"),
                eq("USER"),
                any(),
                any(),
                eq(0),
                eq(50)))
                .thenReturn(logsPage);

        // Act & Assert
        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("userId", userId.toString())
                        .param("action", "USER_SUSPENDED")
                        .param("resourceType", "USER")
                        .param("page", "0")
                        .param("size", "50")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("USER_SUSPENDED"));
    }

    @Test
    void testAdminEndpoint_WithoutAuth_Returns401() throws Exception {
        // Act & Assert - No authentication
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminEndpoint_AsDoctor_Returns403() throws Exception {
        // Act & Assert - Doctor role should not have access
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(user(doctorUser)))
                .andExpect(status().isForbidden());
    }
}

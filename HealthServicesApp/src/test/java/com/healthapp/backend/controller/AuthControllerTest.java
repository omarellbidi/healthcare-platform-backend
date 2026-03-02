package com.healthapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.backend.dto.ApiResponse;
import com.healthapp.backend.dto.LoginRequest;
import com.healthapp.backend.dto.LoginResponse;
import com.healthapp.backend.dto.RegisterRequest;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Tests HTTP endpoints with MockMvc.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest patientRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Patient registration request
        patientRequest = new RegisterRequest();
        patientRequest.setEmail("patient@test.com");
        patientRequest.setPassword("Test1234");
        patientRequest.setRole(Role.PATIENT);
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setPhone("1234567890");
        patientRequest.setGender("Male");
        patientRequest.setDob(LocalDate.of(1990, 1, 1));

        // Login request
        loginRequest = new LoginRequest("test@test.com", "Test1234");
    }

    @Test
    void testRegister_ValidPatientRequest_Returns201() throws Exception {
        // Arrange
        ApiResponse response = new ApiResponse(true, "Registration successful! Please check your email to verify your account.");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful! Please check your email to verify your account."));
    }

    @Test
    void testRegister_InvalidEmail_Returns400() throws Exception {
        // Arrange
        patientRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_WeakPassword_Returns400() throws Exception {
        // Arrange
        patientRequest.setPassword("weak"); // Missing uppercase and number

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_MissingRequiredFields_Returns400() throws Exception {
        // Arrange
        patientRequest.setFirstName(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_ValidCredentials_Returns200WithToken() throws Exception {
        // Arrange
        LoginResponse loginResponse = new LoginResponse(
                "jwt-token-12345",
                UUID.randomUUID(),
                "test@test.com",
                Role.PATIENT
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-12345"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"));
    }

    @Test
    void testLogin_InvalidEmailFormat_Returns400() throws Exception {
        // Arrange
        loginRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_MissingPassword_Returns400() throws Exception {
        // Arrange
        loginRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVerifyEmail_ValidToken_Returns200() throws Exception {
        // Arrange
        ApiResponse response = new ApiResponse(true, "Email verified successfully! You can now login.");
        when(authService.verifyEmail(anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-email")
                        .param("token", "valid-token-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully! You can now login."));
    }

    @Test
    void testForgotPassword_ValidEmail_Returns200() throws Exception {
        // Arrange
        ApiResponse response = new ApiResponse(true, "Password reset link sent to your email");
        when(authService.forgotPassword(anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset link sent to your email"));
    }

    @Test
    void testResetPassword_ValidTokenAndPassword_Returns200() throws Exception {
        // Arrange
        ApiResponse response = new ApiResponse(true, "Password reset successful! You can now login.");
        when(authService.resetPassword(anyString(), anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .param("token", "reset-token-12345")
                        .param("newPassword", "NewPassword1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful! You can now login."));
    }

    @Test
    void testLogout_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void testHealth_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Auth service is running"));
    }

    @Test
    void testRegister_DoctorWithoutLicense_Returns500() throws Exception {
        // Arrange
        RegisterRequest doctorRequest = new RegisterRequest();
        doctorRequest.setEmail("doctor@test.com");
        doctorRequest.setPassword("Test1234");
        doctorRequest.setRole(Role.DOCTOR);
        doctorRequest.setFirstName("Jane");
        doctorRequest.setLastName("Smith");
        doctorRequest.setPhone("0987654321");
        doctorRequest.setGender("Female");
        // Missing licenseNumber and specialization

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("License number is required for doctors"));

        // Act & Assert (IllegalArgumentException results in 500 by GlobalExceptionHandler)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorRequest)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An error occurred: License number is required for doctors"));
    }

    @Test
    void testRegister_ValidDoctorRequest_Returns201() throws Exception {
        // Arrange
        RegisterRequest doctorRequest = new RegisterRequest();
        doctorRequest.setEmail("doctor@test.com");
        doctorRequest.setPassword("Test1234");
        doctorRequest.setRole(Role.DOCTOR);
        doctorRequest.setFirstName("Jane");
        doctorRequest.setLastName("Smith");
        doctorRequest.setPhone("0987654321");
        doctorRequest.setGender("Female");
        doctorRequest.setLicenseNumber("DOC12345");
        doctorRequest.setSpecialization("Cardiology");

        ApiResponse response = new ApiResponse(true, "Registration successful! Please check your email to verify your account.");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}

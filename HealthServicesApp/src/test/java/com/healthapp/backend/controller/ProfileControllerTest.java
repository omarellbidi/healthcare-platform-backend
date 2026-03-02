package com.healthapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.backend.dto.PatientProfileDTO;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.security.UserDetailsImpl;
import com.healthapp.backend.service.DoctorService;
import com.healthapp.backend.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @MockBean
    private DoctorService doctorService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        // Create a mock user with PATIENT role
        User user = new User();
        user.setId(testUserId);
        user.setEmail("patient@test.com");
        user.setRole(Role.PATIENT);
        user.setVerified(true);
        user.setPassword("password");

        // Create UserDetailsImpl using factory method
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // Set up authentication
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getPatientProfile_Success() throws Exception {
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setGender("Male");

        when(patientService.getProfile(any(UUID.class))).thenReturn(dto);

        mockMvc.perform(get("/api/profile/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void updatePatientProfile_Success() throws Exception {
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setDob(LocalDate.of(1995, 5, 5));
        dto.setGender("Female");

        when(patientService.updateProfile(any(UUID.class), any(PatientProfileDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/profile/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void deletePatientProfile_Success() throws Exception {
        mockMvc.perform(delete("/api/profile/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Patient account deleted successfully"));
    }

    @Test
    void getPatientProfile_Unauthorized() throws Exception {
        // Clear authentication to test unauthorized access
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/profile/patient"))
                .andExpect(status().isUnauthorized());
    }
}

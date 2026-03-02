package com.healthapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.backend.dto.AppointmentDTO;
import com.healthapp.backend.dto.BookAppointmentRequest;
import com.healthapp.backend.enums.AppointmentStatus;
import com.healthapp.backend.security.JwtTokenProvider;
import com.healthapp.backend.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AppointmentController.
 * Tests primarily focus on authorization and endpoint availability.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private BookAppointmentRequest bookRequest;

    @BeforeEach
    void setUp() {
        bookRequest = new BookAppointmentRequest();
        bookRequest.setDoctorId(UUID.randomUUID());
        bookRequest.setAppointmentDate(LocalDate.now().plusDays(3));
        bookRequest.setStartTime(LocalTime.of(10, 0));
        bookRequest.setReason("Checkup");
    }

    @Test
    void bookAppointment_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAppointments_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelAppointment_Unauthorized() throws Exception {
        UUID appointmentId = UUID.randomUUID();

        mockMvc.perform(put("/api/appointments/{id}/cancel", appointmentId))
                .andExpect(status().isUnauthorized());
    }
}

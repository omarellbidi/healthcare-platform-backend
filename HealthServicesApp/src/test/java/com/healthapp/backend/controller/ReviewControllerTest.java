package com.healthapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.backend.dto.ReviewDTO;
import com.healthapp.backend.security.JwtTokenProvider;
import com.healthapp.backend.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ReviewController.
 * Tests public endpoints that don't require authentication.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        UUID reviewId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        reviewDTO = new ReviewDTO();
        reviewDTO.setId(reviewId);
        reviewDTO.setAppointmentId(appointmentId);
        reviewDTO.setPatientId(patientId);
        reviewDTO.setPatientName("John Doe");
        reviewDTO.setDoctorId(doctorId);
        reviewDTO.setDoctorName("Dr. Smith");
        reviewDTO.setRating(5);
        reviewDTO.setComment("Excellent doctor!");
        reviewDTO.setCreatedAt(LocalDateTime.now());
        reviewDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getReviewsByDoctor_Success() throws Exception {
        UUID doctorId = UUID.randomUUID();
        Page<ReviewDTO> page = new PageImpl<>(Arrays.asList(reviewDTO));

        when(reviewService.getReviewsByDoctor(eq(doctorId), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/reviews/doctor/{doctorId}", doctorId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].rating").value(5));
    }

    @Test
    void getReviewsByDoctor_DefaultPagination() throws Exception {
        UUID doctorId = UUID.randomUUID();
        Page<ReviewDTO> page = new PageImpl<>(Arrays.asList(reviewDTO));

        when(reviewService.getReviewsByDoctor(eq(doctorId), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reviews/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

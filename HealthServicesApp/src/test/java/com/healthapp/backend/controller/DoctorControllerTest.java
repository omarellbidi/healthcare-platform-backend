package com.healthapp.backend.controller;

import com.healthapp.backend.dto.DoctorPublicProfileDTO;
import com.healthapp.backend.dto.DoctorSearchResponse;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.security.JwtTokenProvider;
import com.healthapp.backend.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DoctorController.
 * Tests public doctor search and profile endpoints (no authentication required).
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private DoctorPublicProfileDTO doctorDTO1;
    private DoctorPublicProfileDTO doctorDTO2;
    private Doctor doctorEntity;

    @BeforeEach
    void setUp() {
        UUID doctorId1 = UUID.randomUUID();
        UUID doctorId2 = UUID.randomUUID();

        doctorDTO1 = new DoctorPublicProfileDTO();
        doctorDTO1.setId(doctorId1);
        doctorDTO1.setFirstName("John");
        doctorDTO1.setLastName("Smith");
        doctorDTO1.setSpecialization("Cardiology");
        doctorDTO1.setExperience(10);
        doctorDTO1.setEducation("MD from Harvard");
        doctorDTO1.setBio("Experienced cardiologist");
        doctorDTO1.setLanguages(Arrays.asList("English", "Spanish"));
        doctorDTO1.setClinicAddress("123 Medical Center");
        doctorDTO1.setAverageRating(4.5);
        doctorDTO1.setReviewCount(20);

        doctorDTO2 = new DoctorPublicProfileDTO();
        doctorDTO2.setId(doctorId2);
        doctorDTO2.setFirstName("Jane");
        doctorDTO2.setLastName("Doe");
        doctorDTO2.setSpecialization("Dermatology");
        doctorDTO2.setExperience(8);
        doctorDTO2.setAverageRating(4.8);
        doctorDTO2.setReviewCount(35);

        // Setup doctor entity for getById endpoint
        User user = new User();
        user.setId(UUID.randomUUID());

        doctorEntity = new Doctor();
        doctorEntity.setId(doctorId1);
        doctorEntity.setUser(user);
        doctorEntity.setFirstName("John");
        doctorEntity.setLastName("Smith");
        doctorEntity.setSpecialization("Cardiology");
        doctorEntity.setExperience(10);
        doctorEntity.setEducation("MD from Harvard");
        doctorEntity.setBio("Experienced cardiologist");
        doctorEntity.setLanguages(Arrays.asList("English", "Spanish"));
        doctorEntity.setClinicAddress("123 Medical Center");
        doctorEntity.setAverageRating(4.5);
        doctorEntity.setReviewCount(20);
    }

    @Test
    void searchDoctors_NoFilters() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO1, doctorDTO2));
        response.setTotalCount(2L);
        response.setCurrentPage(0);
        response.setTotalPages(1);
        response.setPageSize(20);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors").isArray())
                .andExpect(jsonPath("$.doctors.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.doctors[0].firstName").value("John"))
                .andExpect(jsonPath("$.doctors[0].specialization").value("Cardiology"));
    }

    @Test
    void searchDoctors_FilterByName() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO1));
        response.setTotalCount(1L);
        response.setCurrentPage(0);
        response.setTotalPages(1);
        response.setPageSize(20);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search")
                        .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors.length()").value(1))
                .andExpect(jsonPath("$.doctors[0].firstName").value("John"));
    }

    @Test
    void searchDoctors_FilterBySpecialization() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO1));
        response.setTotalCount(1L);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search")
                        .param("specialization", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors[0].specialization").value("Cardiology"));
    }

    @Test
    void searchDoctors_FilterByMinRating() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO2)); // 4.8 rating
        response.setTotalCount(1L);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search")
                        .param("minRating", "4.7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors[0].averageRating").value(4.8));
    }

    @Test
    void searchDoctors_FilterByLanguages() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO1));
        response.setTotalCount(1L);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search")
                        .param("languages", "Spanish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors[0].languages").isArray())
                .andExpect(jsonPath("$.doctors[0].languages[1]").value("Spanish"));
    }

    @Test
    void searchDoctors_FilterByAvailableDate() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO1));
        response.setTotalCount(1L);

        when(searchService.searchDoctors(any())).thenReturn(response);

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/api/doctors/search")
                        .param("availableDate", tomorrow.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors").isArray());
    }

    @Test
    void searchDoctors_SortByRating() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO2, doctorDTO1)); // Sorted by rating desc
        response.setTotalCount(2L);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search")
                        .param("sortBy", "rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors[0].averageRating").value(4.8))
                .andExpect(jsonPath("$.doctors[1].averageRating").value(4.5));
    }

    @Test
    void searchDoctors_WithPagination() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO1));
        response.setTotalCount(10L);
        response.setCurrentPage(0);
        response.setTotalPages(2);
        response.setPageSize(5);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalCount").value(10));
    }

    @Test
    void searchDoctors_EmptyResults() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList());
        response.setTotalCount(0L);
        response.setCurrentPage(0);
        response.setTotalPages(0);
        response.setPageSize(20);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search")
                        .param("specialization", "NonExistentSpecialty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors").isEmpty())
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    @Test
    void getDoctorPublicProfile_Success() throws Exception {
        UUID doctorId = doctorEntity.getId();

        when(doctorRepository.findById(eq(doctorId))).thenReturn(Optional.of(doctorEntity));
        when(searchService.mapToPublicDTO(eq(doctorEntity))).thenReturn(doctorDTO1);

        mockMvc.perform(get("/api/doctors/{id}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.specialization").value("Cardiology"))
                .andExpect(jsonPath("$.experience").value(10))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(20))
                .andExpect(jsonPath("$.languages").isArray())
                .andExpect(jsonPath("$.clinicAddress").value("123 Medical Center"));
    }

    @Test
    void getDoctorPublicProfile_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        when(doctorRepository.findById(eq(nonExistentId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/doctors/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchDoctors_MultipleFilters() throws Exception {
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(Arrays.asList(doctorDTO1));
        response.setTotalCount(1L);

        when(searchService.searchDoctors(any())).thenReturn(response);

        mockMvc.perform(get("/api/doctors/search")
                        .param("specialization", "Cardiology")
                        .param("minRating", "4.0")
                        .param("languages", "Spanish")
                        .param("sortBy", "rating")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors[0].specialization").value("Cardiology"))
                .andExpect(jsonPath("$.doctors[0].averageRating").value(4.5));
    }
}

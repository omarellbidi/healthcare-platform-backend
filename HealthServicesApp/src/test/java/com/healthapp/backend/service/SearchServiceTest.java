package com.healthapp.backend.service;

import com.healthapp.backend.dto.DoctorSearchRequest;
import com.healthapp.backend.dto.DoctorSearchResponse;
import com.healthapp.backend.entity.Availability;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.repository.AvailabilityRepository;
import com.healthapp.backend.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for SearchService covering:
 * - Multi-filter doctor search (name, specialization, rating, languages, availability)
 * - Sorting by rating and experience
 * - Pagination
 * - Language filtering (JSON field)
 * - Availability filtering by date
 */
@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private SearchService searchService;

    private Doctor testDoctor1;
    private Doctor testDoctor2;
    private Doctor testDoctor3;

    @BeforeEach
    void setUp() {
        // Doctor 1 - Cardiologist, speaks English and Spanish, 4.5 rating
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setEmail("doctor1@test.com");

        testDoctor1 = new Doctor();
        testDoctor1.setId(UUID.randomUUID());
        testDoctor1.setUser(user1);
        testDoctor1.setFirstName("John");
        testDoctor1.setLastName("Smith");
        testDoctor1.setSpecialization("Cardiology");
        testDoctor1.setExperience(10);
        testDoctor1.setLanguages(Arrays.asList("English", "Spanish"));
        testDoctor1.setAverageRating(4.5);
        testDoctor1.setReviewCount(20);
        testDoctor1.setApproved(true);

        // Doctor 2 - Dermatologist, speaks English and French, 4.8 rating
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setEmail("doctor2@test.com");

        testDoctor2 = new Doctor();
        testDoctor2.setId(UUID.randomUUID());
        testDoctor2.setUser(user2);
        testDoctor2.setFirstName("Jane");
        testDoctor2.setLastName("Doe");
        testDoctor2.setSpecialization("Dermatology");
        testDoctor2.setExperience(15);
        testDoctor2.setLanguages(Arrays.asList("English", "French"));
        testDoctor2.setAverageRating(4.8);
        testDoctor2.setReviewCount(35);
        testDoctor2.setApproved(true);

        // Doctor 3 - Cardiologist, speaks English only, 3.5 rating
        User user3 = new User();
        user3.setId(UUID.randomUUID());
        user3.setEmail("doctor3@test.com");

        testDoctor3 = new Doctor();
        testDoctor3.setId(UUID.randomUUID());
        testDoctor3.setUser(user3);
        testDoctor3.setFirstName("Bob");
        testDoctor3.setLastName("Johnson");
        testDoctor3.setSpecialization("Cardiology");
        testDoctor3.setExperience(5);
        testDoctor3.setLanguages(Arrays.asList("English"));
        testDoctor3.setAverageRating(3.5);
        testDoctor3.setReviewCount(10);
        testDoctor3.setApproved(true);
    }

    @Test
    void searchDoctors_NoFilters() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setPage(0);
        request.setSize(20);

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor2, testDoctor3);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(3, result.getDoctors().size());
        assertEquals(3, result.getTotalCount());
    }

    @Test
    void searchDoctors_FilterByName() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setName("John");
        request.setPage(0);
        request.setSize(20);

        List<Doctor> doctors = Arrays.asList(testDoctor1);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq("John"), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(1, result.getDoctors().size());
        assertEquals("John", result.getDoctors().get(0).getFirstName());
    }

    @Test
    void searchDoctors_FilterBySpecialization() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setSpecialization("Cardiology");
        request.setPage(0);
        request.setSize(20);

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor3);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq(null), eq("Cardiology"), eq(null), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(2, result.getDoctors().size());
        assertTrue(result.getDoctors().stream()
                .allMatch(d -> d.getSpecialization().equals("Cardiology")));
    }

    @Test
    void searchDoctors_FilterByMinRating() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setMinRating(4.0);
        request.setPage(0);
        request.setSize(20);

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor2);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq(null), eq(null), eq(4.0), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(2, result.getDoctors().size());
        assertTrue(result.getDoctors().stream()
                .allMatch(d -> d.getAverageRating() >= 4.0));
    }

    @Test
    void searchDoctors_FilterByLanguages() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setLanguages(Arrays.asList("Spanish"));
        request.setPage(0);
        request.setSize(20);

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor2, testDoctor3);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(1, result.getDoctors().size());
        assertEquals("John", result.getDoctors().get(0).getFirstName());
        assertTrue(result.getDoctors().get(0).getLanguages().contains("Spanish"));
    }

    @Test
    void searchDoctors_FilterByAvailability() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setAvailableDate(LocalDate.now().plusDays(1)); // Tuesday or whatever day
        request.setPage(0);
        request.setSize(20);

        DayOfWeek dayOfWeek = request.getAvailableDate().getDayOfWeek();

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor2, testDoctor3);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        // Mock availability for doctor1 only
        Availability availability1 = new Availability();
        availability1.setDoctor(testDoctor1);
        availability1.setDayOfWeek(dayOfWeek);
        availability1.setStartTime(LocalTime.of(9, 0));
        availability1.setEndTime(LocalTime.of(17, 0));

        when(availabilityRepository.findByDoctorIdAndDayOfWeek(testDoctor1.getId(), dayOfWeek))
                .thenReturn(Arrays.asList(availability1));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(testDoctor2.getId(), dayOfWeek))
                .thenReturn(Arrays.asList());
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(testDoctor3.getId(), dayOfWeek))
                .thenReturn(Arrays.asList());

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(1, result.getDoctors().size());
        assertEquals("John", result.getDoctors().get(0).getFirstName());
    }

    @Test
    void searchDoctors_SortByRating() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setSortBy("rating");
        request.setPage(0);
        request.setSize(20);

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor2, testDoctor3);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(3, result.getDoctors().size());
        // Should be sorted by rating descending (4.8, 4.5, 3.5)
        assertEquals(4.8, result.getDoctors().get(0).getAverageRating());
        assertEquals(4.5, result.getDoctors().get(1).getAverageRating());
        assertEquals(3.5, result.getDoctors().get(2).getAverageRating());
    }

    @Test
    void searchDoctors_SortByExperience() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setSortBy("experience");
        request.setPage(0);
        request.setSize(20);

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor2, testDoctor3);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(3, result.getDoctors().size());
        // Should be sorted by experience descending (15, 10, 5)
        assertEquals(15, result.getDoctors().get(0).getExperience());
        assertEquals(10, result.getDoctors().get(1).getExperience());
        assertEquals(5, result.getDoctors().get(2).getExperience());
    }

    @Test
    void searchDoctors_CombinedFilters() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setSpecialization("Cardiology");
        request.setMinRating(4.0);
        request.setLanguages(Arrays.asList("Spanish"));
        request.setPage(0);
        request.setSize(20);

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor3);
        Page<Doctor> page = new PageImpl<>(doctors);

        when(doctorRepository.searchDoctors(eq(null), eq("Cardiology"), eq(4.0), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(1, result.getDoctors().size());
        assertEquals("John", result.getDoctors().get(0).getFirstName());
        assertEquals("Cardiology", result.getDoctors().get(0).getSpecialization());
        assertTrue(result.getDoctors().get(0).getAverageRating() >= 4.0);
        assertTrue(result.getDoctors().get(0).getLanguages().contains("Spanish"));
    }

    @Test
    void searchDoctors_Pagination() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setPage(0);
        request.setSize(2);

        List<Doctor> doctors = Arrays.asList(testDoctor1, testDoctor2);
        Page<Doctor> page = new PageImpl<>(doctors, org.springframework.data.domain.PageRequest.of(0, 2), 3);

        when(doctorRepository.searchDoctors(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(2, result.getDoctors().size());
        assertEquals(3, result.getTotalCount());
        assertEquals(0, result.getCurrentPage());
        assertEquals(2, result.getTotalPages());
        assertEquals(2, result.getPageSize());
    }

    @Test
    void searchDoctors_EmptyResults() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setSpecialization("NonExistentSpecialization");
        request.setPage(0);
        request.setSize(20);

        Page<Doctor> page = new PageImpl<>(Arrays.asList());

        when(doctorRepository.searchDoctors(eq(null), eq("NonExistentSpecialization"), eq(null), any(Pageable.class)))
                .thenReturn(page);

        DoctorSearchResponse result = searchService.searchDoctors(request);

        assertNotNull(result);
        assertEquals(0, result.getDoctors().size());
        assertEquals(0, result.getTotalCount());
    }
}

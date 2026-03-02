package com.healthapp.backend.service;

import com.healthapp.backend.dto.DoctorPublicProfileDTO;
import com.healthapp.backend.dto.DoctorSearchRequest;
import com.healthapp.backend.dto.DoctorSearchResponse;
import com.healthapp.backend.entity.Availability;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.repository.AvailabilityRepository;
import com.healthapp.backend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for searching and filtering doctors.
 *
 * Implements complex search with multiple filters:
 * - Name search (fuzzy matching)
 * - Specialization filter
 * - Languages filter (checks if doctor speaks ANY of the requested languages)
 * - Minimum rating filter
 * - Availability on specific date (checks if doctor works on that day)
 * - Pagination and sorting (by rating or experience)
 *
 * Search Algorithm:
 * 1. Apply database filters (name, specialization, rating) via repository
 * 2. Filter by languages in application layer (JSON array field)
 * 3. Filter by availability if date provided
 * 4. Sort results
 * 5. Return paginated response
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final DoctorRepository doctorRepository;
    private final AvailabilityRepository availabilityRepository;

    /**
     * Search doctors with multiple filters and pagination.
     */
    public DoctorSearchResponse searchDoctors(DoctorSearchRequest request) {
        // Set defaults
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "rating";

        // Build sort
        Sort sort = Sort.by(Sort.Direction.DESC,
                sortBy.equals("experience") ? "experience" : "average_rating");
        Pageable pageable = PageRequest.of(page, size, sort);

        // Search with database filters
        Page<Doctor> doctorPage = doctorRepository.searchDoctors(
                request.getName(),
                request.getSpecialization(),
                request.getMinRating(),
                pageable
        );

        // Get all doctors from current page (create mutable list)
        List<Doctor> doctors = new java.util.ArrayList<>(doctorPage.getContent());

        // Filter by languages (application-level filter for JSON field)
        if (request.getLanguages() != null && !request.getLanguages().isEmpty()) {
            doctors = doctors.stream()
                    .filter(doctor -> doctorSpeaksAnyLanguage(doctor, request.getLanguages()))
                    .collect(Collectors.toList());
        }

        // Filter by availability on specific date
        if (request.getAvailableDate() != null) {
            DayOfWeek dayOfWeek = request.getAvailableDate().getDayOfWeek();
            doctors = doctors.stream()
                    .filter(doctor -> isDoctorAvailableOnDay(doctor.getId(), dayOfWeek))
                    .collect(Collectors.toList());
        }

        // Re-apply sorting after filtering (since filtering may change order)
        if (sortBy != null) {
            if (sortBy.equals("rating")) {
                doctors.sort(Comparator.comparing((Doctor d) -> d.getAverageRating() == null ? 0.0 : d.getAverageRating()).reversed());
            } else if (sortBy.equals("experience")) {
                doctors.sort(Comparator.comparing((Doctor d) -> d.getExperience() == null ? 0 : d.getExperience()).reversed());
            }
        }

        // Map to public DTOs
        List<DoctorPublicProfileDTO> doctorDTOs = doctors.stream()
                .map(this::mapToPublicDTO)
                .collect(Collectors.toList());

        // Build response with pagination metadata
        DoctorSearchResponse response = new DoctorSearchResponse();
        response.setDoctors(doctorDTOs);
        response.setTotalCount(doctorPage.getTotalElements());
        response.setCurrentPage(page);
        response.setTotalPages(doctorPage.getTotalPages());
        response.setPageSize(size);

        return response;
    }

    /**
     * Check if doctor speaks any of the requested languages.
     */
    private boolean doctorSpeaksAnyLanguage(Doctor doctor, List<String> requestedLanguages) {
        if (doctor.getLanguages() == null || doctor.getLanguages().isEmpty()) {
            return false;
        }
        return requestedLanguages.stream()
                .anyMatch(lang -> doctor.getLanguages().contains(lang));
    }

    /**
     * Check if doctor works on the specified day of week.
     */
    private boolean isDoctorAvailableOnDay(UUID doctorId, DayOfWeek dayOfWeek) {
        List<Availability> availabilities = availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);
        return !availabilities.isEmpty();
    }

    /**
     * Map Doctor entity to public profile DTO.
     */
    public DoctorPublicProfileDTO mapToPublicDTO(Doctor doctor) {
        DoctorPublicProfileDTO dto = new DoctorPublicProfileDTO();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setExperience(doctor.getExperience());
        dto.setEducation(doctor.getEducation());
        dto.setBio(doctor.getBio());
        dto.setLanguages(doctor.getLanguages());
        dto.setClinicAddress(doctor.getClinicAddress());
        dto.setAverageRating(doctor.getAverageRating() != null ? doctor.getAverageRating() : 0.0);
        dto.setReviewCount(doctor.getReviewCount() != null ? doctor.getReviewCount() : 0);
        dto.setProfilePhotoBase64(doctor.getProfilePhotoBase64());
        return dto;
    }
}

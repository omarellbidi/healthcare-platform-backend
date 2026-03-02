package com.healthapp.backend.controller;

import com.healthapp.backend.dto.DoctorPublicProfileDTO;
import com.healthapp.backend.dto.DoctorSearchRequest;
import com.healthapp.backend.dto.DoctorSearchResponse;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for public doctor search and profile viewing.
 *
 * Provides endpoints for:
 * - Searching doctors with filters (name, specialization, languages, rating, availability)
 * - Viewing public doctor profiles
 *
 * These endpoints are publicly accessible (no authentication required)
 * to allow patients to browse and find doctors before booking.
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Search", description = "Public doctor search and profile viewing")
public class DoctorController {

    private final SearchService searchService;
    private final DoctorRepository doctorRepository;

    /**
     * Search doctors with multiple filters and pagination.
     */
    @GetMapping("/search")
    @Operation(summary = "Search doctors", description = "Search doctors with multiple filters and pagination")
    public ResponseEntity<DoctorSearchResponse> searchDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) java.util.List<String> languages,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate availableDate,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        // Build search request
        DoctorSearchRequest request = new DoctorSearchRequest();
        request.setName(name);
        request.setSpecialization(specialization);
        request.setLanguages(languages);
        request.setMinRating(minRating);
        request.setAvailableDate(availableDate);
        request.setSortBy(sortBy);
        request.setPage(page);
        request.setSize(size);

        DoctorSearchResponse response = searchService.searchDoctors(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get public profile of a specific doctor.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get doctor public profile", description = "Get public profile of a specific doctor")
    public ResponseEntity<DoctorPublicProfileDTO> getDoctorPublicProfile(@PathVariable UUID id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Use SearchService to map DTO (reuses existing mapping logic)
        DoctorPublicProfileDTO dto = searchService.mapToPublicDTO(doctor);

        return ResponseEntity.ok(dto);
    }
}

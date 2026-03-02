package com.healthapp.backend.controller;

import com.healthapp.backend.dto.ApiResponse;
import com.healthapp.backend.dto.DoctorProfileDTO;
import com.healthapp.backend.dto.PatientProfileDTO;
import com.healthapp.backend.security.UserDetailsImpl;
import com.healthapp.backend.service.DoctorService;
import com.healthapp.backend.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for profile management.
 * Handles CRUD operations for patient and doctor profiles.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile Management", description = "Profile CRUD endpoints for patients and doctors")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final PatientService patientService;
    private final DoctorService doctorService;

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient profile", description = "Get current patient's profile")
    public ResponseEntity<PatientProfileDTO> getPatientProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PatientProfileDTO profile = patientService.getProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update patient profile", description = "Update current patient's profile")
    public ResponseEntity<PatientProfileDTO> updatePatientProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PatientProfileDTO dto) {

        PatientProfileDTO updated = patientService.updateProfile(userDetails.getId(), dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Delete patient account", description = "Delete current patient's profile and account")
    public ResponseEntity<ApiResponse> deletePatientProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        patientService.deleteProfile(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Patient account deleted successfully"));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get doctor profile", description = "Get current doctor's profile")
    public ResponseEntity<DoctorProfileDTO> getDoctorProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        DoctorProfileDTO profile = doctorService.getProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update doctor profile", description = "Update current doctor's profile")
    public ResponseEntity<DoctorProfileDTO> updateDoctorProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody DoctorProfileDTO dto) {

        DoctorProfileDTO updated = doctorService.updateProfile(userDetails.getId(), dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete doctor account", description = "Delete current doctor's profile and account")
    public ResponseEntity<ApiResponse> deleteDoctorProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        doctorService.deleteProfile(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Doctor account deleted successfully"));
    }
}

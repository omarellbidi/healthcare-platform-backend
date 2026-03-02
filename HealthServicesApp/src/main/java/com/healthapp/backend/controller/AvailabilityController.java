package com.healthapp.backend.controller;

import com.healthapp.backend.dto.ApiResponse;
import com.healthapp.backend.dto.AvailabilityDTO;
import com.healthapp.backend.dto.TimeSlotDTO;
import com.healthapp.backend.dto.UnavailabilityDTO;
import com.healthapp.backend.security.UserDetailsImpl;
import com.healthapp.backend.service.AvailabilityService;
import com.healthapp.backend.service.UnavailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for availability and unavailability management.
 * Handles doctor schedule management and time slot generation.
 * All endpoints require JWT authentication and DOCTOR role (except time slot viewing).
 */
@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(name = "Availability Management", description = "Doctor availability and schedule management")
@SecurityRequirement(name = "bearerAuth")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final UnavailabilityService unavailabilityService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Set availability", description = "Create new availability schedule (working days and hours)")
    public ResponseEntity<AvailabilityDTO> createAvailability(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody AvailabilityDTO dto) {

        AvailabilityDTO created = availabilityService.createAvailability(userDetails.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get availability", description = "Get doctor's availability schedule")
    public ResponseEntity<List<AvailabilityDTO>> getAvailability(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<AvailabilityDTO> availability = availabilityService.getAvailability(userDetails.getId());
        return ResponseEntity.ok(availability);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update availability", description = "Update existing availability schedule")
    public ResponseEntity<AvailabilityDTO> updateAvailability(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody AvailabilityDTO dto) {

        AvailabilityDTO updated = availabilityService.updateAvailability(userDetails.getId(), id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete availability", description = "Delete availability schedule")
    public ResponseEntity<ApiResponse> deleteAvailability(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {

        availabilityService.deleteAvailability(userDetails.getId(), id);
        return ResponseEntity.ok(new ApiResponse(true, "Availability deleted successfully"));
    }

    @PostMapping("/unavailability")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Mark unavailable", description = "Mark specific dates as unavailable (vacation, sick days, etc.)")
    public ResponseEntity<UnavailabilityDTO> createUnavailability(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UnavailabilityDTO dto) {

        UnavailabilityDTO created = unavailabilityService.createUnavailability(userDetails.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/unavailability")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get unavailable periods", description = "Get doctor's unavailable periods")
    public ResponseEntity<List<UnavailabilityDTO>> getUnavailability(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<UnavailabilityDTO> unavailability = unavailabilityService.getUnavailability(userDetails.getId());
        return ResponseEntity.ok(unavailability);
    }

    @DeleteMapping("/unavailability/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Remove unavailability", description = "Remove unavailable period")
    public ResponseEntity<ApiResponse> deleteUnavailability(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {

        unavailabilityService.deleteUnavailability(userDetails.getId(), id);
        return ResponseEntity.ok(new ApiResponse(true, "Unavailability removed successfully"));
    }

    @GetMapping("/slots/{doctorId}")
    @Operation(summary = "Get available time slots", description = "Get available time slots for a doctor on a specific date")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlotDTO> slots = availabilityService.generateTimeSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }
}

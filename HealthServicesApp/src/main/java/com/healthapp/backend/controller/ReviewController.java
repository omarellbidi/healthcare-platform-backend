package com.healthapp.backend.controller;

import com.healthapp.backend.dto.ApiResponse;
import com.healthapp.backend.dto.CreateReviewRequest;
import com.healthapp.backend.dto.ReviewDTO;
import com.healthapp.backend.security.UserDetailsImpl;
import com.healthapp.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for patient reviews of doctors.
 *
 * Provides endpoints for:
 * - Creating reviews for completed appointments (patients only)
 * - Updating reviews (author only)
 * - Deleting reviews (author only)
 * - Viewing reviews for a doctor (public, paginated)
 *
 * Business rules:
 * - Only completed appointments can be reviewed
 * - One review per appointment
 * - Only review author can update/delete
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Patient reviews and ratings for doctors")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Patient submits review for completed appointment.
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Create review", description = "Patient submits review for completed appointment")
    public ResponseEntity<ReviewDTO> createReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewDTO review = reviewService.createReview(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * Get all reviews for a doctor (paginated).
     */
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor reviews", description = "Get all reviews for a doctor (paginated)")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByDoctor(
            @PathVariable UUID doctorId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        Page<ReviewDTO> reviews = reviewService.getReviewsByDoctor(doctorId, page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Update existing review (author only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update review", description = "Update existing review (author only)")
    public ResponseEntity<ReviewDTO> updateReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewDTO review = reviewService.updateReview(id, userDetails.getId(), request);
        return ResponseEntity.ok(review);
    }

    /**
     * Delete review (author only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Delete review", description = "Delete review (author only)")
    public ResponseEntity<ApiResponse> deleteReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {

        reviewService.deleteReview(id, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Review deleted successfully"));
    }
}

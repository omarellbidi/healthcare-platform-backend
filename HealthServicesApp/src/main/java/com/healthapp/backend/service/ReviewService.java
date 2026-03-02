package com.healthapp.backend.service;

import com.healthapp.backend.dto.CreateReviewRequest;
import com.healthapp.backend.dto.ReviewDTO;
import com.healthapp.backend.entity.Appointment;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.Patient;
import com.healthapp.backend.entity.Review;
import com.healthapp.backend.enums.AppointmentStatus;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.exception.ReviewAlreadyExistsException;
import com.healthapp.backend.repository.AppointmentRepository;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.PatientRepository;
import com.healthapp.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing patient reviews of doctors.
 *
 * Core Functionality:
 * - Create reviews for completed appointments
 * - Update existing reviews (author only)
 * - Delete reviews (author only)
 * - Get reviews for a doctor (paginated)
 * - Automatically update doctor's average rating when reviews change
 *
 * Business Rules Enforced:
 * - Only COMPLETED appointments can be reviewed
 * - One review per appointment (no duplicates)
 * - Only the patient who created the review can update/delete it
 * - Doctor's average rating recalculated after each review operation
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Create a new review for a completed appointment.
     * Validates appointment is completed and hasn't been reviewed yet.
     */
    @Transactional
    public ReviewDTO createReview(UUID patientUserId, CreateReviewRequest request) {
        // Get patient
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        // Get appointment
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Verify patient owns this appointment
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new ResourceNotFoundException("Unauthorized: This appointment does not belong to you");
        }

        // Verify appointment is completed
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ReviewAlreadyExistsException("Can only review completed appointments");
        }

        // Check if appointment already has a review
        if (reviewRepository.existsByAppointmentId(appointment.getId())) {
            throw new ReviewAlreadyExistsException("This appointment has already been reviewed");
        }

        // Create review
        Review review = new Review();
        review.setAppointment(appointment);
        review.setPatient(patient);
        review.setDoctor(appointment.getDoctor());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);

        // Update doctor's average rating
        updateDoctorRating(appointment.getDoctor().getId());

        return mapToDTO(saved);
    }

    /**
     * Update an existing review.
     * Only the author can update their review.
     */
    @Transactional
    public ReviewDTO updateReview(UUID reviewId, UUID patientUserId, CreateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Verify patient owns this review
        if (!review.getPatient().getUser().getId().equals(patientUserId)) {
            throw new ResourceNotFoundException("Unauthorized: You can only update your own reviews");
        }

        // Update review
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updated = reviewRepository.save(review);

        // Update doctor's average rating
        updateDoctorRating(review.getDoctor().getId());

        return mapToDTO(updated);
    }

    /**
     * Delete a review.
     * Only the author can delete their review.
     */
    @Transactional
    public void deleteReview(UUID reviewId, UUID patientUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Verify patient owns this review
        if (!review.getPatient().getUser().getId().equals(patientUserId)) {
            throw new ResourceNotFoundException("Unauthorized: You can only delete your own reviews");
        }

        UUID doctorId = review.getDoctor().getId();
        reviewRepository.delete(review);

        // Update doctor's average rating
        updateDoctorRating(doctorId);
    }

    /**
     * Get all reviews for a doctor (paginated, sorted by newest first).
     */
    public Page<ReviewDTO> getReviewsByDoctor(UUID doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId, pageable);
        return reviewPage.map(this::mapToDTO);
    }

    /**
     * Get single review by ID.
     */
    public ReviewDTO getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return mapToDTO(review);
    }

    /**
     * Update doctor's average rating and review count.
     * Called automatically after creating, updating, or deleting a review.
     */
    private void updateDoctorRating(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Calculate average rating
        Double averageRating = reviewRepository.calculateAverageRating(doctorId);
        long reviewCount = reviewRepository.countByDoctorId(doctorId);

        // Update doctor
        doctor.setAverageRating(averageRating != null ? averageRating : 0.0);
        doctor.setReviewCount((int) reviewCount);
        doctorRepository.save(doctor);
    }

    /**
     * Map Review entity to DTO.
     */
    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setAppointmentId(review.getAppointment().getId());

        // Patient info
        dto.setPatientId(review.getPatient().getId());
        dto.setPatientName(review.getPatient().getFirstName() + " " + review.getPatient().getLastName());

        // Doctor info
        dto.setDoctorId(review.getDoctor().getId());
        dto.setDoctorName(review.getDoctor().getFirstName() + " " + review.getDoctor().getLastName());

        // Review content
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        return dto;
    }
}

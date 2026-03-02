package com.healthapp.backend.repository;

import com.healthapp.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Review entity with custom queries.
 *
 * Provides methods for:
 * - Checking if appointment already has review (one review per appointment rule)
 * - Finding reviews by doctor (with pagination)
 * - Calculating average rating for doctors
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Check if appointment already has a review (one review per appointment)
    boolean existsByAppointmentId(UUID appointmentId);

    // Find review by appointment (for retrieval)
    Optional<Review> findByAppointmentId(UUID appointmentId);

    // Find all reviews for a doctor (paginated, sorted by newest first)
    Page<Review> findByDoctorIdOrderByCreatedAtDesc(UUID doctorId, Pageable pageable);

    // Calculate average rating for a doctor
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :doctorId")
    Double calculateAverageRating(@Param("doctorId") UUID doctorId);

    // Count reviews for a doctor
    long countByDoctorId(UUID doctorId);
}

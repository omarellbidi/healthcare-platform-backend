package com.healthapp.backend.repository;

import com.healthapp.backend.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    Optional<Doctor> findByUserId(UUID userId);

    // Admin feature methods
    long countByApproved(boolean approved);
    Page<Doctor> findByApproved(boolean approved, Pageable pageable);

    // Search feature
    @Query("SELECT d FROM Doctor d WHERE d.approved = true " +
           "AND (:name IS NULL OR LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
           "AND (:minRating IS NULL OR d.averageRating >= :minRating)")
    Page<Doctor> searchDoctors(@Param("name") String name,
                                @Param("specialization") String specialization,
                                @Param("minRating") Double minRating,
                                Pageable pageable);
}
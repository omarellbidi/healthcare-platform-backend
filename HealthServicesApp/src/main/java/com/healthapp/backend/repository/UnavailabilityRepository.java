package com.healthapp.backend.repository;

import com.healthapp.backend.entity.Unavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UnavailabilityRepository extends JpaRepository<Unavailability, UUID> {
    List<Unavailability> findByDoctorId(UUID doctorId);
}

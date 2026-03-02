package com.healthapp.backend.service;

import com.healthapp.backend.dto.UnavailabilityDTO;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.Unavailability;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.UnavailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing doctor unavailability periods.
 * Handles CRUD operations on unavailable dates (vacations, sick days, etc.).
 */
@Service
@RequiredArgsConstructor
public class UnavailabilityService {

    private final UnavailabilityRepository unavailabilityRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Create new unavailability period for a doctor.
     * @param userId Doctor's user ID
     * @param dto Unavailability data
     * @return Created UnavailabilityDTO
     */
    @Transactional
    public UnavailabilityDTO createUnavailability(UUID userId, UnavailabilityDTO dto) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Unavailability unavailability = new Unavailability();
        unavailability.setDoctor(doctor);
        unavailability.setStartDate(dto.getStartDate());
        unavailability.setEndDate(dto.getEndDate());
        unavailability.setReason(dto.getReason());

        Unavailability saved = unavailabilityRepository.save(unavailability);
        return mapToDTO(saved);
    }

    /**
     * Get all unavailability periods for a doctor.
     * @param userId Doctor's user ID
     * @return List of UnavailabilityDTO
     */
    public List<UnavailabilityDTO> getUnavailability(UUID userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return unavailabilityRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete unavailability period.
     * @param userId Doctor's user ID
     * @param unavailabilityId Unavailability ID to delete
     */
    @Transactional
    public void deleteUnavailability(UUID userId, UUID unavailabilityId) {
        Unavailability unavailability = unavailabilityRepository.findById(unavailabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Unavailability not found"));

        // Verify this unavailability belongs to the doctor
        if (!unavailability.getDoctor().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Unauthorized: This unavailability does not belong to you");
        }

        unavailabilityRepository.delete(unavailability);
    }

    /**
     * Map Unavailability entity to DTO.
     */
    private UnavailabilityDTO mapToDTO(Unavailability unavailability) {
        UnavailabilityDTO dto = new UnavailabilityDTO();
        dto.setId(unavailability.getId());
        dto.setStartDate(unavailability.getStartDate());
        dto.setEndDate(unavailability.getEndDate());
        dto.setReason(unavailability.getReason());
        return dto;
    }
}

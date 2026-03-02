package com.healthapp.backend.service;

import com.healthapp.backend.dto.PatientProfileDTO;
import com.healthapp.backend.entity.Patient;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.repository.PatientRepository;
import com.healthapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing patient profiles.
 * Handles CRUD operations on patient data.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    /**
     * Get patient profile by user ID.
     * @param userId The user's UUID
     * @return PatientProfileDTO with profile data
     */
    public PatientProfileDTO getProfile(UUID userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return mapToDTO(patient);
    }

    /**
     * Update patient profile.
     * @param userId The user's UUID
     * @param dto Updated profile data
     * @return Updated PatientProfileDTO
     */
    @Transactional
    public PatientProfileDTO updateProfile(UUID userId, PatientProfileDTO dto) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setDob(dto.getDob());
        patient.setPhone(dto.getPhone());
        patient.setAddress(dto.getAddress());
        patient.setGender(dto.getGender());
        patient.setMedicalHistory(dto.getMedicalHistory());
        patient.setProfilePhotoBase64(dto.getProfilePhotoBase64());
        patient.setInsuranceInfo(dto.getInsuranceInfo());

        Patient saved = patientRepository.save(patient);
        return mapToDTO(saved);
    }

    /**
     * Delete patient profile and associated user account.
     * @param userId The user's UUID
     */
    @Transactional
    public void deleteProfile(UUID userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        patientRepository.delete(patient);
        userRepository.delete(user);
    }

    /**
     * Map Patient entity to DTO.
     */
    private PatientProfileDTO mapToDTO(Patient patient) {
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setDob(patient.getDob());
        dto.setPhone(patient.getPhone());
        dto.setAddress(patient.getAddress());
        dto.setGender(patient.getGender());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setProfilePhotoBase64(patient.getProfilePhotoBase64());
        dto.setInsuranceInfo(patient.getInsuranceInfo());
        return dto;
    }
}

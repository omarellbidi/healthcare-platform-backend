package com.healthapp.backend.service;

import com.healthapp.backend.dto.DoctorProfileDTO;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing doctor profiles.
 * Handles CRUD operations on doctor data.
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    /**
     * Get doctor profile by user ID.
     * @param userId The user's UUID
     * @return DoctorProfileDTO with profile data
     */
    public DoctorProfileDTO getProfile(UUID userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        return mapToDTO(doctor);
    }

    /**
     * Update doctor profile.
     * @param userId The user's UUID
     * @param dto Updated profile data
     * @return Updated DoctorProfileDTO
     */
    @Transactional
    public DoctorProfileDTO updateProfile(UUID userId, DoctorProfileDTO dto) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setPhone(dto.getPhone());
        doctor.setGender(dto.getGender());
        doctor.setProfilePhotoBase64(dto.getProfilePhotoBase64());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setExperience(dto.getExperience());
        doctor.setEducation(dto.getEducation());
        doctor.setBio(dto.getBio());
        doctor.setLanguages(dto.getLanguages());
        doctor.setClinicAddress(dto.getClinicAddress());

        Doctor saved = doctorRepository.save(doctor);
        return mapToDTO(saved);
    }

    /**
     * Delete doctor profile and associated user account.
     * @param userId The user's UUID
     */
    @Transactional
    public void deleteProfile(UUID userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        doctorRepository.delete(doctor);
        userRepository.delete(user);
    }

    /**
     * Map Doctor entity to DTO.
     */
    private DoctorProfileDTO mapToDTO(Doctor doctor) {
        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setPhone(doctor.getPhone());
        dto.setGender(doctor.getGender());
        dto.setProfilePhotoBase64(doctor.getProfilePhotoBase64());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setExperience(doctor.getExperience());
        dto.setEducation(doctor.getEducation());
        dto.setBio(doctor.getBio());
        dto.setLanguages(doctor.getLanguages());
        dto.setClinicAddress(doctor.getClinicAddress());
        dto.setAverageRating(doctor.getAverageRating() != null ? doctor.getAverageRating() : 0.0);
        dto.setReviewCount(doctor.getReviewCount() != null ? doctor.getReviewCount() : 0);
        return dto;
    }
}

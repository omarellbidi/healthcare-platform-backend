package com.healthapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for doctor profile data transfer.
 * Used for GET and PUT operations on doctor profiles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileDTO {

    private String firstName;
    private String lastName;
    private String phone;
    private String gender;
    private String profilePhotoBase64;
    private String licenseNumber;
    private String specialization;
    private Integer experience;
    private String education;
    private String bio;
    private List<String> languages;
    private String clinicAddress;
    private Double averageRating;
    private Integer reviewCount;
}

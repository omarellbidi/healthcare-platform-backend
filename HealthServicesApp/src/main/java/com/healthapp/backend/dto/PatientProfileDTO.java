package com.healthapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for patient profile data transfer.
 * Used for GET and PUT operations on patient profiles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileDTO {

    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String phone;
    private String address;
    private String gender;
    private Map<String, Object> medicalHistory; // JSON questionnaire from frontend
    private String profilePhotoBase64;
    private String insuranceInfo;
}

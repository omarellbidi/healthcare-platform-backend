package com.healthapp.backend.dto;

import com.healthapp.backend.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Registration request DTO for both patient and doctor registration.
 * Contains common fields (email, password, name, phone, gender) and role-specific optional fields.
 * Validation rules ensure password strength (8+ chars, 1 uppercase, 1 number) and required fields based on role.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters with 1 uppercase letter and 1 number"
    )
    private String password; // Password validation: min 8 chars, 1 uppercase, 1 digit

    @NotNull(message = "Role is required")
    private Role role; // PATIENT or DOCTOR - determines which profile fields are required

    // Common fields for both roles
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Gender is required")
    private String gender;

    // Patient-specific fields (dob is required for patients, validated in AuthService)
    private LocalDate dob; // Date of birth - required for PATIENT role
    private String address;
    private String profilePhotoBase64; // Base64-encoded image
    private String insuranceInfo;
    private Map<String, Object> medicalQuestionnaire; // Flexible JSONB structure for medical history

    // Doctor-specific fields (licenseNumber and specialization required, validated in AuthService)
    private String licenseNumber; // Medical license - required for DOCTOR role
    private String specialization; // Medical specialization - required for DOCTOR role
    private Integer experience; // Years of experience
    private String education;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio; // Professional biography (max 500 chars)

    private List<String> languages; // Languages spoken
    private String clinicAddress;
}
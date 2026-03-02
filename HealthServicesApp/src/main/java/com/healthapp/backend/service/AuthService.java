package com.healthapp.backend.service;

import com.healthapp.backend.dto.*;
import com.healthapp.backend.entity.*;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.exception.*;
import com.healthapp.backend.repository.*;
import com.healthapp.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication service handling user registration, login, email verification, and password reset.
 * Supports role-based registration for patients and doctors with separate profile creation workflows.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    /**
     * Registers a new user (patient or doctor) and sends email verification.
     * Creates role-specific profile and generates 24-hour verification token.
     */
    @Transactional
    public ApiResponse register(RegisterRequest request) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Validate role-specific required fields
        if (request.getRole() == Role.PATIENT && request.getDob() == null) {
            throw new IllegalArgumentException("Date of birth is required for patients");
        }

        if (request.getRole() == Role.DOCTOR && request.getLicenseNumber() == null) {
            throw new IllegalArgumentException("License number is required for doctors");
        }

        if (request.getRole() == Role.DOCTOR && request.getSpecialization() == null) {
            throw new IllegalArgumentException("Specialization is required for doctors");
        }

        // Create User entity with hashed password
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // bcrypt hashing
        user.setRole(request.getRole());
        user.setVerified(false); // Requires email verification before login

        // Generate verification token (expires in 24 hours)
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));

        // Save user first to get ID for profile relationship
        user = userRepository.save(user);

        // Create role-specific profile (Patient or Doctor)
        if (request.getRole() == Role.PATIENT) {
            createPatientProfile(user, request);
        } else if (request.getRole() == Role.DOCTOR) {
            createDoctorProfile(user, request);
        }

        // Send verification email asynchronously
        emailService.sendVerificationEmail(
                user.getEmail(),
                verificationToken
        );

        return new ApiResponse(
                true,
                "Registration successful! Please check your email to verify your account."
        );
    }

    /**
     * Creates patient profile and optional medical history questionnaire.
     * Medical questionnaire uses flexible JSONB storage for any custom structure.
     */
    private void createPatientProfile(User user, RegisterRequest request) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDob(request.getDob());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setProfilePhotoBase64(request.getProfilePhotoBase64());
        patient.setInsuranceInfo(request.getInsuranceInfo());

        patientRepository.save(patient);

        // Create medical history with flexible questionnaire if provided
        if (request.getMedicalQuestionnaire() != null && !request.getMedicalQuestionnaire().isEmpty()) {
            MedicalHistory medicalHistory = new MedicalHistory();
            medicalHistory.setPatient(patient);
            medicalHistory.setQuestionnaireJson(request.getMedicalQuestionnaire()); // Stores any JSON structure
            medicalHistoryRepository.save(medicalHistory);
        }
    }

    /**
     * Creates doctor profile with professional credentials.
     * Doctor is created with approved=false and requires admin approval to practice.
     */
    private void createDoctorProfile(User user, RegisterRequest request) {
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setPhone(request.getPhone());
        doctor.setGender(request.getGender());
        doctor.setProfilePhotoBase64(request.getProfilePhotoBase64());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setExperience(request.getExperience());
        doctor.setEducation(request.getEducation());
        doctor.setBio(request.getBio());
        doctor.setLanguages(request.getLanguages());
        doctor.setClinicAddress(request.getClinicAddress());
        doctor.setApproved(false);

        doctorRepository.save(doctor);
    }

    /**
     * Verifies user email using the token sent during registration.
     * Token must be valid and not expired (24-hour expiration). Sends welcome email on success.
     */
    @Transactional
    public ApiResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        // Check if token expired (24-hour validity)
        if (user.getTokenExpiryDate() == null || user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token has expired");
        }

        // Mark user as verified and clear verification token
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        // Send welcome email asynchronously
        String firstName = getFirstName(user);
        emailService.sendWelcomeEmail(user.getEmail(), firstName);

        return new ApiResponse(true, "Email verified successfully! You can now login.");
    }

    /**
     * Authenticates user and returns JWT token.
     * User must have verified their email before login is allowed.
     */
    public LoginResponse login(LoginRequest request) {
        // Authenticate credentials using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Retrieve user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        // Enforce email verification requirement
        if (!user.getVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }

        // Generate JWT token (24-hour expiration)
        String token = tokenProvider.generateToken(authentication);

        return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole());
    }

    /**
     * Initiates password reset process by sending reset token via email.
     * Token expires in 24 hours.
     */
    @Transactional
    public ApiResponse forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Send email
        emailService.sendPasswordResetEmail(email, resetToken);

        return new ApiResponse(true, "Password reset link sent to your email");
    }

    /**
     * Resets user password using the token sent via email.
     * Token must be valid and not expired. Password is hashed with bcrypt before storage.
     */
    @Transactional
    public ApiResponse resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        // Check token expiration (24-hour validity)
        if (user.getTokenExpiryDate() == null || user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Reset token has expired");
        }

        // Update password with bcrypt hashing and clear reset token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        return new ApiResponse(true, "Password reset successful! You can now login.");
    }

    /**
     * Helper method to extract first name from user's role-specific profile.
     */
    private String getFirstName(User user) {
        if (user.getPatient() != null) {
            return user.getPatient().getFirstName();
        } else if (user.getDoctor() != null) {
            return user.getDoctor().getFirstName();
        }
        return "User";
    }
}

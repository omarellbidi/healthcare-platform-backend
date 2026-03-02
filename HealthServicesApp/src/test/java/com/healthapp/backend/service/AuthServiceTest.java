package com.healthapp.backend.service;

import com.healthapp.backend.dto.ApiResponse;
import com.healthapp.backend.dto.LoginRequest;
import com.healthapp.backend.dto.LoginResponse;
import com.healthapp.backend.dto.RegisterRequest;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.Patient;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.exception.*;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.MedicalHistoryRepository;
import com.healthapp.backend.repository.PatientRepository;
import com.healthapp.backend.repository.UserRepository;
import com.healthapp.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests registration, login, email verification, and password reset functionality.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private MedicalHistoryRepository medicalHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest patientRequest;
    private RegisterRequest doctorRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Patient registration request
        patientRequest = new RegisterRequest();
        patientRequest.setEmail("patient@test.com");
        patientRequest.setPassword("Test1234");
        patientRequest.setRole(Role.PATIENT);
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setPhone("1234567890");
        patientRequest.setGender("Male");
        patientRequest.setDob(LocalDate.of(1990, 1, 1));

        // Doctor registration request
        doctorRequest = new RegisterRequest();
        doctorRequest.setEmail("doctor@test.com");
        doctorRequest.setPassword("Test1234");
        doctorRequest.setRole(Role.DOCTOR);
        doctorRequest.setFirstName("Jane");
        doctorRequest.setLastName("Smith");
        doctorRequest.setPhone("0987654321");
        doctorRequest.setGender("Female");
        doctorRequest.setLicenseNumber("DOC12345");
        doctorRequest.setSpecialization("Cardiology");

        // Test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@test.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.PATIENT);
        testUser.setVerified(true);
    }

    @Test
    void testRegisterPatient_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(patientRepository.save(any(Patient.class))).thenReturn(new Patient());
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act
        ApiResponse response = authService.register(patientRequest);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Registration successful! Please check your email to verify your account.", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testRegisterDoctor_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(new Doctor());
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act
        ApiResponse response = authService.register(doctorRequest);

        // Assert
        assertTrue(response.getSuccess());
        verify(doctorRepository).save(any(Doctor.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("patient@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(patientRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_PatientWithoutDob_ThrowsException() {
        // Arrange
        patientRequest.setDob(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.register(patientRequest));
    }

    @Test
    void testRegister_DoctorWithoutLicense_ThrowsException() {
        // Arrange
        doctorRequest.setLicenseNumber(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.register(doctorRequest));
    }

    @Test
    void testVerifyEmail_ValidToken_Success() {
        // Arrange
        User unverifiedUser = new User();
        unverifiedUser.setEmail("test@test.com");
        unverifiedUser.setVerified(false);
        unverifiedUser.setVerificationToken("valid-token");
        unverifiedUser.setTokenExpiryDate(LocalDateTime.now().plusHours(1));

        Patient patient = new Patient();
        patient.setFirstName("John");
        unverifiedUser.setPatient(patient);

        when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(unverifiedUser));
        when(userRepository.save(any(User.class))).thenReturn(unverifiedUser);
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // Act
        ApiResponse response = authService.verifyEmail("valid-token");

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Email verified successfully! You can now login.", response.getMessage());
        assertTrue(unverifiedUser.getVerified());
        assertNull(unverifiedUser.getVerificationToken());
        verify(emailService).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    void testVerifyEmail_InvalidToken_ThrowsException() {
        // Arrange
        when(userRepository.findByVerificationToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> authService.verifyEmail("invalid-token"));
    }

    @Test
    void testVerifyEmail_ExpiredToken_ThrowsException() {
        // Arrange
        User user = new User();
        user.setVerificationToken("expired-token");
        user.setTokenExpiryDate(LocalDateTime.now().minusHours(1)); // Expired

        when(userRepository.findByVerificationToken("expired-token")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(TokenExpiredException.class, () -> authService.verifyEmail("expired-token"));
    }

    @Test
    void testLogin_ValidCredentials_ReturnsToken() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@test.com", "Test1234");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getRole(), response.getRole());
    }

    @Test
    void testLogin_UnverifiedEmail_ThrowsException() {
        // Arrange
        testUser.setVerified(false);
        LoginRequest loginRequest = new LoginRequest("test@test.com", "Test1234");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(EmailNotVerifiedException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@test.com", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    void testForgotPassword_ValidEmail_SendsResetEmail() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        // Act
        ApiResponse response = authService.forgotPassword("test@test.com");

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Password reset link sent to your email", response.getMessage());
        assertNotNull(testUser.getResetToken());
        verify(emailService).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void testForgotPassword_InvalidEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> authService.forgotPassword("nonexistent@test.com"));
    }

    @Test
    void testResetPassword_ValidToken_ChangesPassword() {
        // Arrange
        testUser.setResetToken("reset-token");
        testUser.setTokenExpiryDate(LocalDateTime.now().plusHours(1));

        when(userRepository.findByResetToken("reset-token")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ApiResponse response = authService.resetPassword("reset-token", "NewPassword1");

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Password reset successful! You can now login.", response.getMessage());
        assertEquals("newEncodedPassword", testUser.getPassword());
        assertNull(testUser.getResetToken());
        assertNull(testUser.getTokenExpiryDate());
    }

    @Test
    void testResetPassword_ExpiredToken_ThrowsException() {
        // Arrange
        testUser.setResetToken("reset-token");
        testUser.setTokenExpiryDate(LocalDateTime.now().minusHours(1)); // Expired

        when(userRepository.findByResetToken("reset-token")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(TokenExpiredException.class,
                () -> authService.resetPassword("reset-token", "NewPassword1"));
    }

    @Test
    void testResetPassword_InvalidToken_ThrowsException() {
        // Arrange
        when(userRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class,
                () -> authService.resetPassword("invalid-token", "NewPassword1"));
    }
}

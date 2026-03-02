package com.healthapp.backend.security;

import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtTokenProvider.
 * Tests JWT token generation, validation, and email extraction.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Use reflection to set private fields for testing
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION);
    }

    @Test
    void testGenerateToken_ValidAuthentication_ReturnsToken() {
        // Arrange
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setRole(Role.PATIENT);
        user.setVerified(true);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setRole(Role.PATIENT);
        user.setVerified(true);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "notajwttoken";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Arrange
        String emptyToken = "";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGetEmailFromToken_ValidToken_ReturnsEmail() {
        // Arrange
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setRole(Role.PATIENT);
        user.setVerified(true);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String email = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals("test@test.com", email);
    }

    @Test
    void testValidateToken_TokenWithDifferentSecret_ReturnsFalse() {
        // Arrange - Generate token with one secret
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setRole(Role.PATIENT);
        user.setVerified(true);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);

        // Change the secret
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "differentSecretKey1234567890123456789012345678901234567890");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGenerateToken_DifferentRoles_GeneratesTokens() {
        // Test PATIENT role
        User patient = new User();
        patient.setId(UUID.randomUUID());
        patient.setEmail("patient@test.com");
        patient.setPassword("password");
        patient.setRole(Role.PATIENT);
        patient.setVerified(true);

        UserDetailsImpl patientDetails = UserDetailsImpl.build(patient);
        Authentication patientAuth = mock(Authentication.class);
        when(patientAuth.getPrincipal()).thenReturn(patientDetails);

        String patientToken = jwtTokenProvider.generateToken(patientAuth);

        // Test DOCTOR role
        User doctor = new User();
        doctor.setId(UUID.randomUUID());
        doctor.setEmail("doctor@test.com");
        doctor.setPassword("password");
        doctor.setRole(Role.DOCTOR);
        doctor.setVerified(true);

        UserDetailsImpl doctorDetails = UserDetailsImpl.build(doctor);
        Authentication doctorAuth = mock(Authentication.class);
        when(doctorAuth.getPrincipal()).thenReturn(doctorDetails);

        String doctorToken = jwtTokenProvider.generateToken(doctorAuth);

        // Assert
        assertNotNull(patientToken);
        assertNotNull(doctorToken);
        assertNotEquals(patientToken, doctorToken); // Different tokens for different users
        assertTrue(jwtTokenProvider.validateToken(patientToken));
        assertTrue(jwtTokenProvider.validateToken(doctorToken));
    }
}

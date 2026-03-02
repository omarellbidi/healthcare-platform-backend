package com.healthapp.backend.security;

import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserDetailsServiceImpl.
 * Tests user loading for Spring Security authentication.
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@test.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.PATIENT);
        testUser.setVerified(true);
    }

    @Test
    void testLoadUserByUsername_ExistingUser_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@test.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled()); // User is verified
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PATIENT")));
    }

    @Test
    void testLoadUserByUsername_NonExistingUser_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent@test.com")
        );
        assertTrue(exception.getMessage().contains("User not found with email"));
    }

    @Test
    void testLoadUserByUsername_UnverifiedUser_ReturnsDisabledUser() {
        // Arrange
        testUser.setVerified(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled()); // Unverified users are disabled
    }

    @Test
    void testLoadUserByUsername_DoctorRole_ReturnsCorrectAuthority() {
        // Arrange
        testUser.setRole(Role.DOCTOR);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_DOCTOR")));
    }

    @Test
    void testLoadUserByUsername_AdminRole_ReturnsCorrectAuthority() {
        // Arrange
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_ReturnsUserDetailsImpl() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Assert
        assertInstanceOf(UserDetailsImpl.class, userDetails);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        assertEquals(testUser.getId(), userDetailsImpl.getId());
        assertEquals(testUser.getEmail(), userDetailsImpl.getEmail());
        assertEquals(testUser.getRole(), userDetailsImpl.getRole());
    }

    @Test
    void testLoadUserByUsername_AccountNonExpired_ReturnsTrue() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Assert
        assertTrue(userDetails.isAccountNonExpired());
    }

    @Test
    void testLoadUserByUsername_AccountNonLocked_ReturnsTrue() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Assert
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void testLoadUserByUsername_CredentialsNonExpired_ReturnsTrue() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Assert
        assertTrue(userDetails.isCredentialsNonExpired());
    }
}

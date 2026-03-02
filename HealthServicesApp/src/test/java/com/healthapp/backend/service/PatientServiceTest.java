package com.healthapp.backend.service;

import com.healthapp.backend.dto.PatientProfileDTO;
import com.healthapp.backend.entity.Patient;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.repository.PatientRepository;
import com.healthapp.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    private UUID userId;
    private User testUser;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("patient@test.com");

        testPatient = new Patient();
        testPatient.setId(UUID.randomUUID());
        testPatient.setUser(testUser);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setDob(LocalDate.of(1990, 1, 1));
        testPatient.setPhone("1234567890");
        testPatient.setAddress("123 Main St");
        testPatient.setGender("Male");

        Map<String, Object> medicalHistory = new HashMap<>();
        medicalHistory.put("bloodType", "O+");
        testPatient.setMedicalHistory(medicalHistory);
    }

    @Test
    void getProfile_Success() {
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(testPatient));

        PatientProfileDTO result = patientService.getProfile(userId);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Male", result.getGender());
        verify(patientRepository).findByUserId(userId);
    }

    @Test
    void getProfile_NotFound() {
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> patientService.getProfile(userId));
    }

    @Test
    void updateProfile_Success() {
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setDob(LocalDate.of(1995, 5, 5));
        dto.setPhone("0987654321");
        dto.setAddress("456 Oak Ave");
        dto.setGender("Female");

        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        PatientProfileDTO result = patientService.updateProfile(userId, dto);

        assertNotNull(result);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void deleteProfile_Success() {
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(testPatient));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> patientService.deleteProfile(userId));

        verify(patientRepository).delete(testPatient);
        verify(userRepository).delete(testUser);
    }
}

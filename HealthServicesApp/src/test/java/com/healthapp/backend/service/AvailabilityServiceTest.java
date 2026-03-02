package com.healthapp.backend.service;

import com.healthapp.backend.dto.AvailabilityDTO;
import com.healthapp.backend.dto.TimeSlotDTO;
import com.healthapp.backend.entity.Availability;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.Unavailability;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.repository.AvailabilityRepository;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.UnavailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UnavailabilityRepository unavailabilityRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private UUID userId;
    private UUID doctorId;
    private Doctor testDoctor;
    private Availability testAvailability;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        User testUser = new User();
        testUser.setId(userId);

        testDoctor = new Doctor();
        testDoctor.setId(doctorId);
        testDoctor.setUser(testUser);
        testDoctor.setFirstName("Dr. Smith");

        testAvailability = new Availability();
        testAvailability.setId(UUID.randomUUID());
        testAvailability.setDoctor(testDoctor);
        testAvailability.setDayOfWeek(DayOfWeek.MONDAY);
        testAvailability.setStartTime(LocalTime.of(9, 0));
        testAvailability.setEndTime(LocalTime.of(17, 0));
        testAvailability.setBreakStartTime(LocalTime.of(12, 0));
        testAvailability.setBreakEndTime(LocalTime.of(13, 0));
        testAvailability.setSlotDurationMinutes(30);
    }

    @Test
    void createAvailability_Success() {
        AvailabilityDTO dto = new AvailabilityDTO();
        dto.setDayOfWeek(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(17, 0));
        dto.setSlotDurationMinutes(30);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(testDoctor));
        when(availabilityRepository.save(any(Availability.class))).thenReturn(testAvailability);

        AvailabilityDTO result = availabilityService.createAvailability(userId, dto);

        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
        verify(availabilityRepository).save(any(Availability.class));
    }

    @Test
    void generateTimeSlots_Success() {
        LocalDate testDate = LocalDate.of(2024, 1, 8); // A Monday

        List<Availability> availabilities = List.of(testAvailability);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, DayOfWeek.MONDAY))
                .thenReturn(availabilities);
        when(unavailabilityRepository.findByDoctorId(doctorId)).thenReturn(new ArrayList<>());

        List<TimeSlotDTO> slots = availabilityService.generateTimeSlots(doctorId, testDate);

        assertNotNull(slots);
        assertFalse(slots.isEmpty());

        // Should generate slots from 9:00-12:00 and 13:00-17:00 (skipping lunch break)
        // Expected: 9:00, 9:30, 10:00, 10:30, 11:00, 11:30, 13:00, 13:30, 14:00, 14:30, 15:00, 15:30, 16:00, 16:30
        assertTrue(slots.size() > 0);

        // Verify first slot starts at 9:00
        assertEquals(LocalTime.of(9, 0), slots.get(0).getStartTime());

        // Verify no slots during lunch break (12:00-13:00)
        boolean hasLunchBreakSlot = slots.stream()
                .anyMatch(slot -> slot.getStartTime().equals(LocalTime.of(12, 0)));
        assertFalse(hasLunchBreakSlot);
    }

    @Test
    void generateTimeSlots_DoctorUnavailable() {
        LocalDate testDate = LocalDate.of(2024, 1, 8);

        Unavailability vacation = new Unavailability();
        vacation.setDoctor(testDoctor);
        vacation.setStartDate(LocalDate.of(2024, 1, 7));
        vacation.setEndDate(LocalDate.of(2024, 1, 10));
        vacation.setReason("Vacation");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, DayOfWeek.MONDAY))
                .thenReturn(List.of(testAvailability));
        when(unavailabilityRepository.findByDoctorId(doctorId)).thenReturn(List.of(vacation));

        List<TimeSlotDTO> slots = availabilityService.generateTimeSlots(doctorId, testDate);

        // Should return empty list because doctor is unavailable
        assertTrue(slots.isEmpty());
    }

    @Test
    void generateTimeSlots_NoWorkingDay() {
        LocalDate testDate = LocalDate.of(2024, 1, 9); // A Tuesday

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, DayOfWeek.TUESDAY))
                .thenReturn(new ArrayList<>()); // No availability for Tuesday

        List<TimeSlotDTO> slots = availabilityService.generateTimeSlots(doctorId, testDate);

        assertTrue(slots.isEmpty());
    }

    @Test
    void deleteAvailability_Success() {
        UUID availabilityId = testAvailability.getId();

        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(testAvailability));

        assertDoesNotThrow(() -> availabilityService.deleteAvailability(userId, availabilityId));

        verify(availabilityRepository).delete(testAvailability);
    }

    @Test
    void deleteAvailability_Unauthorized() {
        UUID availabilityId = testAvailability.getId();
        UUID differentUserId = UUID.randomUUID();

        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(testAvailability));

        assertThrows(RuntimeException.class,
                () -> availabilityService.deleteAvailability(differentUserId, availabilityId));
    }
}

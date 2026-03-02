package com.healthapp.backend.service;

import com.healthapp.backend.dto.AvailabilityDTO;
import com.healthapp.backend.dto.TimeSlotDTO;
import com.healthapp.backend.entity.Availability;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.Unavailability;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.repository.AvailabilityRepository;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.UnavailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing doctor availability schedules.
 * Handles CRUD operations on availability and time slot generation.
 */
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final UnavailabilityRepository unavailabilityRepository;

    /**
     * Create new availability schedule for a doctor.
     * @param userId Doctor's user ID
     * @param dto Availability data
     * @return Created AvailabilityDTO
     */
    @Transactional
    public AvailabilityDTO createAvailability(UUID userId, AvailabilityDTO dto) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Availability availability = new Availability();
        availability.setDoctor(doctor);
        availability.setDayOfWeek(dto.getDayOfWeek());
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());
        availability.setBreakStartTime(dto.getBreakStartTime());
        availability.setBreakEndTime(dto.getBreakEndTime());
        availability.setSlotDurationMinutes(dto.getSlotDurationMinutes());

        Availability saved = availabilityRepository.save(availability);
        return mapToDTO(saved);
    }

    /**
     * Get all availability schedules for a doctor.
     * @param userId Doctor's user ID
     * @return List of AvailabilityDTO
     */
    public List<AvailabilityDTO> getAvailability(UUID userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return availabilityRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update existing availability schedule.
     * @param userId Doctor's user ID
     * @param availabilityId Availability ID to update
     * @param dto Updated availability data
     * @return Updated AvailabilityDTO
     */
    @Transactional
    public AvailabilityDTO updateAvailability(UUID userId, UUID availabilityId, AvailabilityDTO dto) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        // Verify this availability belongs to the doctor
        if (!availability.getDoctor().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Unauthorized: This availability does not belong to you");
        }

        availability.setDayOfWeek(dto.getDayOfWeek());
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());
        availability.setBreakStartTime(dto.getBreakStartTime());
        availability.setBreakEndTime(dto.getBreakEndTime());
        availability.setSlotDurationMinutes(dto.getSlotDurationMinutes());

        Availability saved = availabilityRepository.save(availability);
        return mapToDTO(saved);
    }

    /**
     * Delete availability schedule.
     * @param userId Doctor's user ID
     * @param availabilityId Availability ID to delete
     */
    @Transactional
    public void deleteAvailability(UUID userId, UUID availabilityId) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        // Verify this availability belongs to the doctor
        if (!availability.getDoctor().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Unauthorized: This availability does not belong to you");
        }

        availabilityRepository.delete(availability);
    }

    /**
     * Generate available time slots for a doctor on a specific date.
     * This is THE ALGORITHM that generates bookable time slots.
     *
     * @param doctorId Doctor's ID
     * @param date Date to generate slots for
     * @return List of TimeSlotDTO with available time slots
     */
    public List<TimeSlotDTO> generateTimeSlots(UUID doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Get doctor's availability for this day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Availability> availabilities = availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

        if (availabilities.isEmpty()) {
            return new ArrayList<>(); // Doctor doesn't work on this day
        }

        // Check if doctor is unavailable on this date
        List<Unavailability> unavailabilities = unavailabilityRepository.findByDoctorId(doctorId);
        boolean isUnavailable = unavailabilities.stream()
                .anyMatch(u -> !date.isBefore(u.getStartDate()) && !date.isAfter(u.getEndDate()));

        if (isUnavailable) {
            return new ArrayList<>(); // Doctor is on vacation/unavailable
        }

        // Generate time slots from availability schedule
        List<TimeSlotDTO> timeSlots = new ArrayList<>();
        for (Availability availability : availabilities) {
            timeSlots.addAll(generateSlotsForAvailability(availability, date));
        }

        return timeSlots;
    }

    /**
     * Helper method to generate time slots from a single availability schedule.
     * This handles the actual slot generation logic.
     */
    private List<TimeSlotDTO> generateSlotsForAvailability(Availability availability, LocalDate date) {
        List<TimeSlotDTO> slots = new ArrayList<>();

        LocalTime currentTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();
        int slotDuration = availability.getSlotDurationMinutes();

        while (currentTime.plusMinutes(slotDuration).isBefore(endTime) ||
               currentTime.plusMinutes(slotDuration).equals(endTime)) {

            LocalTime slotEnd = currentTime.plusMinutes(slotDuration);

            // Skip if slot overlaps with break time
            boolean overlapsWithBreak = false;
            if (availability.getBreakStartTime() != null && availability.getBreakEndTime() != null) {
                overlapsWithBreak = slotEnd.isAfter(availability.getBreakStartTime()) &&
                                    currentTime.isBefore(availability.getBreakEndTime());
            }

            if (!overlapsWithBreak) {
                TimeSlotDTO slot = new TimeSlotDTO();
                slot.setDate(date);
                slot.setStartTime(currentTime);
                slot.setEndTime(slotEnd);
                slot.setAvailable(true); // TODO: Check against appointments in M3
                slots.add(slot);
            }

            currentTime = slotEnd;
        }

        return slots;
    }

    /**
     * Map Availability entity to DTO.
     */
    private AvailabilityDTO mapToDTO(Availability availability) {
        AvailabilityDTO dto = new AvailabilityDTO();
        dto.setId(availability.getId());
        dto.setDayOfWeek(availability.getDayOfWeek());
        dto.setStartTime(availability.getStartTime());
        dto.setEndTime(availability.getEndTime());
        dto.setBreakStartTime(availability.getBreakStartTime());
        dto.setBreakEndTime(availability.getBreakEndTime());
        dto.setSlotDurationMinutes(availability.getSlotDurationMinutes());
        return dto;
    }
}

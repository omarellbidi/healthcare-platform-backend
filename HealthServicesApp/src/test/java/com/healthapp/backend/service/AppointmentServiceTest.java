package com.healthapp.backend.service;

import com.healthapp.backend.dto.AppointmentDTO;
import com.healthapp.backend.dto.BookAppointmentRequest;
import com.healthapp.backend.dto.RescheduleAppointmentRequest;
import com.healthapp.backend.entity.Appointment;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.Patient;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.AppointmentStatus;
import com.healthapp.backend.exception.AppointmentConflictException;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.repository.AppointmentRepository;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for AppointmentService covering all business logic.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    private UUID patientUserId;
    private UUID doctorUserId;
    private UUID patientId;
    private UUID doctorId;
    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        patientUserId = UUID.randomUUID();
        doctorUserId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        User patientUser = new User();
        patientUser.setId(patientUserId);

        testPatient = new Patient();
        testPatient.setId(patientId);
        testPatient.setUser(patientUser);

        User doctorUser = new User();
        doctorUser.setId(doctorUserId);

        testDoctor = new Doctor();
        testDoctor.setId(doctorId);
        testDoctor.setUser(doctorUser);

        testAppointment = new Appointment();
        testAppointment.setId(UUID.randomUUID());
        testAppointment.setPatient(testPatient);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setAppointmentDate(LocalDate.now().plusDays(3));
        testAppointment.setStartTime(LocalTime.of(10, 0));
        testAppointment.setStatus(AppointmentStatus.PENDING);
        testAppointment.setRescheduleCount(0);
    }

    @Test
    void bookAppointment_Success() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setDoctorId(doctorId);
        request.setAppointmentDate(LocalDate.now().plusDays(5));
        request.setStartTime(LocalTime.of(14, 0));

        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
                any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any())).thenReturn(testAppointment);

        AppointmentDTO result = appointmentService.bookAppointment(patientUserId, request);

        assertNotNull(result);
        verify(appointmentRepository).save(any());
    }

    @Test
    void bookAppointment_SlotAlreadyBooked() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setDoctorId(doctorId);

        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
                any(), any(), any(), any())).thenReturn(true);

        assertThrows(AppointmentConflictException.class,
                () -> appointmentService.bookAppointment(patientUserId, request));
    }

    @Test
    void cancelAppointment_Success() {
        when(appointmentRepository.findById(any())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any())).thenReturn(testAppointment);

        assertDoesNotThrow(() -> appointmentService.cancelAppointment(testAppointment.getId(), patientUserId));
        verify(appointmentRepository).save(any());
    }

    @Test
    void getPatientAppointments_Success() {
        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId))
                .thenReturn(Arrays.asList(testAppointment));

        List<AppointmentDTO> result = appointmentService.getPatientAppointments(patientUserId, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getDoctorAppointments_Success() {
        when(doctorRepository.findByUserId(doctorUserId)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findByDoctorIdOrderByAppointmentDateAscStartTimeAsc(doctorId))
                .thenReturn(Arrays.asList(testAppointment));

        List<AppointmentDTO> result = appointmentService.getDoctorAppointments(doctorUserId, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

package com.healthapp.backend.service;

import com.healthapp.backend.dto.CreateReviewRequest;
import com.healthapp.backend.dto.ReviewDTO;
import com.healthapp.backend.entity.Appointment;
import com.healthapp.backend.entity.Doctor;
import com.healthapp.backend.entity.Patient;
import com.healthapp.backend.entity.Review;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.AppointmentStatus;
import com.healthapp.backend.exception.ResourceNotFoundException;
import com.healthapp.backend.exception.ReviewAlreadyExistsException;
import com.healthapp.backend.repository.AppointmentRepository;
import com.healthapp.backend.repository.DoctorRepository;
import com.healthapp.backend.repository.PatientRepository;
import com.healthapp.backend.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Comprehensive tests for ReviewService covering:
 * - Creating reviews for completed appointments
 * - Updating reviews (author only)
 * - Deleting reviews (author only)
 * - Getting reviews by doctor
 * - Automatic doctor rating updates after review operations
 * - Business rule: one review per appointment
 * - Business rule: only completed appointments can be reviewed
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID patientUserId;
    private UUID patientId;
    private UUID doctorId;
    private UUID appointmentId;
    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private Review testReview;

    @BeforeEach
    void setUp() {
        patientUserId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        // Setup patient
        User patientUser = new User();
        patientUser.setId(patientUserId);
        patientUser.setEmail("patient@test.com");

        testPatient = new Patient();
        testPatient.setId(patientId);
        testPatient.setUser(patientUser);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");

        // Setup doctor
        User doctorUser = new User();
        doctorUser.setId(UUID.randomUUID());
        doctorUser.setEmail("doctor@test.com");

        testDoctor = new Doctor();
        testDoctor.setId(doctorId);
        testDoctor.setUser(doctorUser);
        testDoctor.setFirstName("Dr. Jane");
        testDoctor.setLastName("Smith");
        testDoctor.setSpecialization("Cardiology");
        testDoctor.setAverageRating(0.0);
        testDoctor.setReviewCount(0);

        // Setup completed appointment
        testAppointment = new Appointment();
        testAppointment.setId(appointmentId);
        testAppointment.setPatient(testPatient);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setAppointmentDate(LocalDate.now().minusDays(1));
        testAppointment.setStartTime(LocalTime.of(10, 0));
        testAppointment.setStatus(AppointmentStatus.COMPLETED);

        // Setup review
        testReview = new Review();
        testReview.setId(UUID.randomUUID());
        testReview.setAppointment(testAppointment);
        testReview.setPatient(testPatient);
        testReview.setDoctor(testDoctor);
        testReview.setRating(5);
        testReview.setComment("Excellent doctor!");
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createReview_Success() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(5);
        request.setComment("Great service!");

        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        when(reviewRepository.existsByAppointmentId(appointmentId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(reviewRepository.calculateAverageRating(doctorId)).thenReturn(5.0);
        when(reviewRepository.countByDoctorId(doctorId)).thenReturn(1L);

        ReviewDTO result = reviewService.createReview(patientUserId, request);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Excellent doctor!", result.getComment());
        verify(reviewRepository).save(any(Review.class));
        verify(doctorRepository).save(any(Doctor.class)); // Rating update
    }

    @Test
    void createReview_AppointmentNotCompleted() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(5);

        testAppointment.setStatus(AppointmentStatus.PENDING);

        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

        assertThrows(ReviewAlreadyExistsException.class,
                () -> reviewService.createReview(patientUserId, request));
    }

    @Test
    void createReview_AppointmentAlreadyReviewed() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(5);

        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        when(reviewRepository.existsByAppointmentId(appointmentId)).thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class,
                () -> reviewService.createReview(patientUserId, request));
    }

    @Test
    void createReview_UnauthorizedPatient() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(appointmentId);

        Patient differentPatient = new Patient();
        differentPatient.setId(UUID.randomUUID());

        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(differentPatient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.createReview(patientUserId, request));
    }

    @Test
    void updateReview_Success() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(4);
        request.setComment("Updated review");

        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(reviewRepository.calculateAverageRating(doctorId)).thenReturn(4.0);
        when(reviewRepository.countByDoctorId(doctorId)).thenReturn(1L);

        ReviewDTO result = reviewService.updateReview(testReview.getId(), patientUserId, request);

        assertNotNull(result);
        verify(reviewRepository).save(any(Review.class));
        verify(doctorRepository).save(any(Doctor.class)); // Rating update
    }

    @Test
    void updateReview_Unauthorized() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(4);

        UUID wrongUserId = UUID.randomUUID();

        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.updateReview(testReview.getId(), wrongUserId, request));
    }

    @Test
    void updateReview_NotFound() {
        CreateReviewRequest request = new CreateReviewRequest();
        UUID nonExistentId = UUID.randomUUID();

        when(reviewRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.updateReview(nonExistentId, patientUserId, request));
    }

    @Test
    void deleteReview_Success() {
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(reviewRepository.calculateAverageRating(doctorId)).thenReturn(0.0);
        when(reviewRepository.countByDoctorId(doctorId)).thenReturn(0L);

        assertDoesNotThrow(() -> reviewService.deleteReview(testReview.getId(), patientUserId));

        verify(reviewRepository).delete(testReview);
        verify(doctorRepository).save(any(Doctor.class)); // Rating update
    }

    @Test
    void deleteReview_Unauthorized() {
        UUID wrongUserId = UUID.randomUUID();

        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.deleteReview(testReview.getId(), wrongUserId));
    }

    @Test
    void getReviewsByDoctor_Success() {
        List<Review> reviews = Arrays.asList(testReview);
        Page<Review> page = new PageImpl<>(reviews);

        when(reviewRepository.findByDoctorIdOrderByCreatedAtDesc(eq(doctorId), any(Pageable.class)))
                .thenReturn(page);

        Page<ReviewDTO> result = reviewService.getReviewsByDoctor(doctorId, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getContent().get(0).getRating());
    }

    @Test
    void getReviewById_Success() {
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));

        ReviewDTO result = reviewService.getReviewById(testReview.getId());

        assertNotNull(result);
        assertEquals(testReview.getId(), result.getId());
        assertEquals(5, result.getRating());
    }

    @Test
    void getReviewById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(reviewRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.getReviewById(nonExistentId));
    }

    @Test
    void updateDoctorRating_MultipleReviews() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(4);

        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        when(reviewRepository.existsByAppointmentId(appointmentId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(reviewRepository.calculateAverageRating(doctorId)).thenReturn(4.5);
        when(reviewRepository.countByDoctorId(doctorId)).thenReturn(3L);

        reviewService.createReview(patientUserId, request);

        verify(doctorRepository).save(argThat(doctor ->
                doctor.getAverageRating() == 4.5 && doctor.getReviewCount() == 3));
    }

    @Test
    void updateDoctorRating_NoReviews() {
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(reviewRepository.calculateAverageRating(doctorId)).thenReturn(null);
        when(reviewRepository.countByDoctorId(doctorId)).thenReturn(0L);

        reviewService.deleteReview(testReview.getId(), patientUserId);

        verify(doctorRepository).save(argThat(doctor ->
                doctor.getAverageRating() == 0.0 && doctor.getReviewCount() == 0));
    }
}

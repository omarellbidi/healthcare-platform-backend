package com.healthapp.backend.entity;

import com.healthapp.backend.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core user entity for authentication and authorization.
 * Stores login credentials, email verification tokens, password reset tokens, and role-based access control.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // bcrypt hashed via Spring Security

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // PATIENT, DOCTOR, or ADMIN - determines access permissions

    @Column(nullable = false)
    private Boolean verified = false; // Email verification status - users must verify before login

    @Column(nullable = false)
    private Boolean suspended = false; // Account suspension status - admins can suspend users

    @Column(length = 500)
    private String verificationToken; // Token sent via email for account verification

    @Column(length = 500)
    private String resetToken; // Token sent via email for password reset

    private LocalDateTime tokenExpiryDate; // Expiration for both verification and reset tokens (24 hours)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Bidirectional relationship - only one will be populated based on role
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Patient patient; // Populated if role is PATIENT

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Doctor doctor; // Populated if role is DOCTOR
}
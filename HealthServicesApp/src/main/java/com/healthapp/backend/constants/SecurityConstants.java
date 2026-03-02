package com.healthapp.backend.constants;

/**
 * Security-related constants for role-based access control.
 * These constants are used in @PreAuthorize annotations.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    /**
     * Spring Security expression for PATIENT role authorization.
     */
    public static final String ROLE_PATIENT = "hasRole('PATIENT')";

    /**
     * Spring Security expression for DOCTOR role authorization.
     */
    public static final String ROLE_DOCTOR = "hasRole('DOCTOR')";

    /**
     * Spring Security expression for ADMIN role authorization.
     */
    public static final String ROLE_ADMIN = "hasRole('ADMIN')";
}

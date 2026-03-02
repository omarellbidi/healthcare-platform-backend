package com.healthapp.backend.enums;

/**
 * User role enumeration for role-based access control (RBAC).
 * Determines permissions and accessible endpoints in the application.
 */
public enum Role {
    PATIENT,  // Regular patients - can book appointments and manage their own profile
    DOCTOR,   // Medical practitioners - can manage appointments and patient records
    ADMIN     // System administrators - can approve doctors and manage all users
}
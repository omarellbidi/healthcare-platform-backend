package com.healthapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for searching doctors with filters.
 *
 * Supports multiple search criteria:
 * - Name search (fuzzy matching on first/last name)
 * - Specialization filter
 * - Languages spoken filter
 * - Minimum rating filter
 * - Availability on specific date
 * - Pagination (page number and size)
 * - Sorting (by rating or experience)
 *
 * All filters are optional - if not provided, no filtering is applied.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSearchRequest {

    private String name; // Search by doctor name (first or last name)

    private String specialization; // Filter by specialization (e.g., "Cardiology")

    private List<String> languages; // Filter by languages spoken (e.g., ["English", "Spanish"])

    private Double minRating; // Minimum average rating (e.g., 4.0)

    private LocalDate availableDate; // Filter by availability on specific date

    private String sortBy; // Sort field: "rating" or "experience" (default: rating)

    private Integer page; // Page number (0-indexed, default: 0)

    private Integer size; // Page size (default: 20)
}

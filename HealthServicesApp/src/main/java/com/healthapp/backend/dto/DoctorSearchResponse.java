package com.healthapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for doctor search results with pagination.
 *
 * Contains:
 * - List of matching doctors
 * - Pagination metadata (total count, current page, total pages)
 *
 * Used to return paginated search results to frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSearchResponse {

    private List<DoctorPublicProfileDTO> doctors; // List of matching doctors

    private long totalCount; // Total number of doctors matching filters

    private int currentPage; // Current page number (0-indexed)

    private int totalPages; // Total number of pages

    private int pageSize; // Number of results per page
}

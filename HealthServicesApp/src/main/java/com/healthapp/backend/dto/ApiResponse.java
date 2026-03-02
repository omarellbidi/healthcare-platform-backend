package com.healthapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for consistent response format.
 * Used for success/error responses across all endpoints (except login which returns LoginResponse).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Boolean success; // true for successful operations, false for errors
    private String message; // Human-readable message describing the result
    private T data; // Optional data payload (rarely used, mostly just success + message)

    /**
     * Constructor for simple success/error responses without data payload.
     */
    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Static factory method for success responses with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * Static factory method for success responses with custom message and data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Static factory method for error responses.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
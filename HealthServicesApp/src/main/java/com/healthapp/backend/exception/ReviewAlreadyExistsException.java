package com.healthapp.backend.exception;

/**
 * Exception thrown when attempting to create a duplicate review.
 *
 * Business rule: One review per appointment.
 * If patient tries to review the same appointment twice, this exception is thrown.
 */
public class ReviewAlreadyExistsException extends RuntimeException {

    public ReviewAlreadyExistsException(String message) {
        super(message);
    }
}

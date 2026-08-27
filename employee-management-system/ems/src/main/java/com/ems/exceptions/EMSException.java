package com.ems.exceptions;

/**
 * Base checked exception for all Employee Management System errors.
 * Kept as a common parent so callers can catch every domain error with
 * a single catch block when they don't need to distinguish the cause.
 */
public class EMSException extends Exception {
    public EMSException(String message) {
        super(message);
    }

    public EMSException(String message, Throwable cause) {
        super(message, cause);
    }
}

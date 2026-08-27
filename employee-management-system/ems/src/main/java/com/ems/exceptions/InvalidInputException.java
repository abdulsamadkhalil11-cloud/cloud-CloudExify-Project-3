package com.ems.exceptions;

/** Thrown for a menu choice or free-form input that fails validation. */
public class InvalidInputException extends EMSException {
    public InvalidInputException(String message) {
        super(message);
    }
}

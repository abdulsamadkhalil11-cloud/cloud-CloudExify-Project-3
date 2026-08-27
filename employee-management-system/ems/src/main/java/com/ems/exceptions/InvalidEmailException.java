package com.ems.exceptions;

/** Thrown when an email address does not match a valid pattern. */
public class InvalidEmailException extends EMSException {
    public InvalidEmailException(String email) {
        super("'" + email + "' is not a valid email address.");
    }
}

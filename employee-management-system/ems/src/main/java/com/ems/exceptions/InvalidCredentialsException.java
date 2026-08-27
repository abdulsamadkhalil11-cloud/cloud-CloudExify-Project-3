package com.ems.exceptions;

/** Thrown when a username/password combination fails authentication. */
public class InvalidCredentialsException extends EMSException {
    public InvalidCredentialsException() {
        super("Invalid username or password.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

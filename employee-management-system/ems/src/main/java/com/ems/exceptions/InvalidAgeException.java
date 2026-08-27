package com.ems.exceptions;

/** Thrown when an employee's age is outside the allowed 18-65 range. */
public class InvalidAgeException extends EMSException {
    public InvalidAgeException(int age) {
        super("Age " + age + " is outside the allowed range (18-65).");
    }
}

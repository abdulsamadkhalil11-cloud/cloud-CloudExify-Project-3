package com.ems.exceptions;

/** Thrown when a required field is blank. */
public class EmptyFieldException extends EMSException {
    public EmptyFieldException(String fieldName) {
        super("Field '" + fieldName + "' is required and cannot be empty.");
    }
}

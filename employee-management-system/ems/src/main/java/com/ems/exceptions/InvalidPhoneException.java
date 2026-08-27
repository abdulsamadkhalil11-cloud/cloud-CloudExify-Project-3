package com.ems.exceptions;

/** Thrown when a phone number does not match a valid pattern. */
public class InvalidPhoneException extends EMSException {
    public InvalidPhoneException(String phone) {
        super("'" + phone + "' is not a valid phone number.");
    }
}

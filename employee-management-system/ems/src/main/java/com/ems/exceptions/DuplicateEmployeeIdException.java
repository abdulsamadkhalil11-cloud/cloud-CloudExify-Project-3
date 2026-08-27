package com.ems.exceptions;

/** Thrown when an employee is added with an ID that already exists. */
public class DuplicateEmployeeIdException extends EMSException {
    public DuplicateEmployeeIdException(String employeeId) {
        super("An employee with ID '" + employeeId + "' already exists.");
    }
}

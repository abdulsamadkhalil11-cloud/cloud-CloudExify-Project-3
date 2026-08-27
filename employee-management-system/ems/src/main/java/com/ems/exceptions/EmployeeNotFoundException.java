package com.ems.exceptions;

/** Thrown when a lookup by employee ID (or other key) finds no match. */
public class EmployeeNotFoundException extends EMSException {
    public EmployeeNotFoundException(String employeeId) {
        super("No employee found with ID '" + employeeId + "'.");
    }
}

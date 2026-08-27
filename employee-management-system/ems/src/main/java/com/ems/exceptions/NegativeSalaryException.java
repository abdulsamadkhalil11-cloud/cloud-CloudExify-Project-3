package com.ems.exceptions;

/** Thrown when a salary, bonus, or deduction amount would be negative. */
public class NegativeSalaryException extends EMSException {
    public NegativeSalaryException(double value) {
        super("Salary-related amount cannot be negative: " + value);
    }
}

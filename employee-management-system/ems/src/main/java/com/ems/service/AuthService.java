package com.ems.service;

import com.ems.exceptions.InvalidCredentialsException;
import com.ems.model.Admin;
import com.ems.model.Employee;

import java.util.List;

/**
 * Thin wrapper around the login/password behaviour already on
 * Employee and Admin (both implement Loginable). Centralises the
 * "find the account, then authenticate it" step and the
 * forgot-password flow so the console layer doesn't duplicate it.
 */
public class AuthService {

    public Employee authenticateEmployee(List<Employee> roster, String employeeId, String password)
            throws InvalidCredentialsException {
        Employee match = roster.stream()
                .filter(e -> e.getEmployeeId().equalsIgnoreCase(employeeId))
                .findFirst()
                .orElseThrow(InvalidCredentialsException::new);
        match.login(match.getUsername(), password);
        return match;
    }

    public void authenticateAdmin(Admin admin, String username, String password) throws InvalidCredentialsException {
        admin.login(username, password);
    }

    /** Change password after re-verifying the current one (reuses login() as the check). */
    public void changePassword(Employee employee, String oldPassword, String newPassword)
            throws InvalidCredentialsException {
        employee.login(employee.getUsername(), oldPassword);
        employee.changePassword(newPassword);
    }

    /**
     * Forgot-password flow: identity is verified with the CNIC on file
     * (something only the real employee/HR would know) before issuing
     * a new temporary password.
     */
    public String forgotPassword(Employee employee, String cnicConfirmation) throws InvalidCredentialsException {
        if (!employee.getCnic().equalsIgnoreCase(cnicConfirmation)) {
            throw new InvalidCredentialsException("CNIC does not match our records for this employee.");
        }
        String tempPassword = "Reset@" + (int) (Math.random() * 9000 + 1000);
        employee.changePassword(tempPassword);
        return tempPassword;
    }
}

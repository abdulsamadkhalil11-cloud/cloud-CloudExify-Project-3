package com.ems.model;

import com.ems.exceptions.InvalidCredentialsException;
import com.ems.interfaces.Loginable;
import com.ems.util.PasswordUtil;

import java.io.Serializable;

/**
 * System administrator account. Deliberately NOT part of the Employee
 * inheritance tree - an Admin is a system login, not a member of
 * staff with a salary or a department. Admin's "capabilities"
 * (manage employees/managers/developers/HR, backup, restore, view
 * statistics, generate reports) are exercised through the service
 * layer once logged in; they live here only as a login + password
 * reset since those are the two things that belong to the account
 * itself rather than to whichever employee record is being acted on.
 */
public class Admin implements Loginable, Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private String passwordHash;
    private transient boolean loggedIn = false;

    public Admin(String username, String plainPassword) {
        this.username = username;
        this.passwordHash = PasswordUtil.hash(plainPassword);
    }

    @Override
    public boolean login(String username, String password) throws InvalidCredentialsException {
        if (!this.username.equals(username) || !PasswordUtil.verify(password, this.passwordHash)) {
            throw new InvalidCredentialsException();
        }
        loggedIn = true;
        return true;
    }

    @Override
    public void logout() {
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void changePassword(String newPlainPassword) {
        this.passwordHash = PasswordUtil.hash(newPlainPassword);
    }

    /** Admin capability: reset any employee's password to a temporary value. */
    public String resetPassword(Employee employee) {
        String tempPassword = "Temp@" + (int) (Math.random() * 9000 + 1000);
        employee.changePassword(tempPassword);
        return tempPassword;
    }

    public String getUsername() {
        return username;
    }
}

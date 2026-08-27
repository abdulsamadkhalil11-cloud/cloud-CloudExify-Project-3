package com.ems.interfaces;

import com.ems.exceptions.InvalidCredentialsException;

/**
 * Contract for any account type that can authenticate into the system
 * (implemented by both Employee and Admin).
 */
public interface Loginable {
    boolean login(String username, String password) throws InvalidCredentialsException;
    void logout();
}

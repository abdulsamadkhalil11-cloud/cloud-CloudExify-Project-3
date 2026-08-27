package com.ems.util;

import java.util.regex.Pattern;

/**
 * Static validation helpers. Grouped in one class so every part of the
 * system (console input, service layer, tests) checks data the same way.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,13}$");

    private static final Pattern CNIC_PATTERN =
            Pattern.compile("^\\d{5}-\\d{7}-\\d{1}$");

    private Validator() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidCnic(String cnic) {
        return cnic != null && CNIC_PATTERN.matcher(cnic.trim()).matches();
    }

    public static boolean isValidAge(int age) {
        return age >= Constants.MIN_AGE && age <= Constants.MAX_AGE;
    }

    public static boolean isValidSalary(double salary) {
        return salary >= 0;
    }

    /** Overload: validate a single field is non-blank. */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /** Overload: validate that every field in a batch is non-blank. */
    public static boolean isNotEmpty(String... values) {
        if (values == null) {
            return false;
        }
        for (String value : values) {
            if (!isNotEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * A "strong" password: at least 8 characters, with at least one
     * letter, one digit, and one special character.
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        return hasLetter && hasDigit && hasSpecial;
    }
}

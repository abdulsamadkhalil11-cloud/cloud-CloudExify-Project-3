package com.ems.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Wraps a single shared Scanner and re-prompts until the input is
 * valid, so menu code never has to write its own retry loops.
 */
public class InputHelper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (Validator.isNotEmpty(value)) {
                return value;
            }
            System.out.println("  This field can't be empty.");
        }
    }

    /** Lets the caller allow a blank answer to mean "skip / keep current value". */
    public String readOptionalString(String prompt) {
        return readLine(prompt);
    }

    public int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a whole number.");
            }
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("  Enter a number between " + min + " and " + max + ".");
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number.");
            }
        }
    }

    public double readNonNegativeDouble(String prompt) {
        while (true) {
            double value = readDouble(prompt);
            if (value >= 0) {
                return value;
            }
            System.out.println("  This value can't be negative.");
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readLine(prompt + " (yyyy-MM-dd): "), DATE_FMT);
            } catch (Exception e) {
                System.out.println("  Please use the format yyyy-MM-dd, e.g. 2026-08-03.");
            }
        }
    }

    public YearMonth readMonth(String prompt) {
        while (true) {
            try {
                return YearMonth.parse(readLine(prompt + " (yyyy-MM): "), MONTH_FMT);
            } catch (Exception e) {
                System.out.println("  Please use the format yyyy-MM, e.g. 2026-08.");
            }
        }
    }

    public String readEmail(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (Validator.isValidEmail(value)) {
                return value;
            }
            System.out.println("  That doesn't look like a valid email address.");
        }
    }

    public String readPhone(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (Validator.isValidPhone(value)) {
                return value;
            }
            System.out.println("  Phone should be 10-13 digits, optionally starting with +.");
        }
    }

    public String readCnic(String prompt) {
        while (true) {
            String value = readLine(prompt + " (format 00000-0000000-0): ");
            if (Validator.isValidCnic(value)) {
                return value;
            }
            System.out.println("  CNIC must look like 00000-0000000-0.");
        }
    }

    /**
     * Console input can't truly mask characters without a native
     * terminal, so the password is echoed as typed - a known
     * limitation of a plain System.in console app.
     */
    public String readStrongPassword(String prompt) {
        while (true) {
            String value = readLine(prompt + " (min 8 chars, letter+digit+symbol): ");
            if (Validator.isStrongPassword(value)) {
                return value;
            }
            System.out.println("  Password too weak - needs a letter, a digit, a symbol, 8+ chars.");
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String value = readLine(prompt + " (y/n): ").toLowerCase();
            if (value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("n") || value.equals("no")) {
                return false;
            }
            System.out.println("  Please answer y or n.");
        }
    }

    public void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}

package com.ems.util;

import java.time.LocalTime;
import java.time.MonthDay;
import java.util.List;

/**
 * Central place for tunable constants used across the system.
 * Everything here is static and final on purpose: these are fixed
 * company policy numbers, not per-employee state.
 */
public final class Constants {

    // Prevent instantiation - this is a pure constant holder.
    private Constants() {
    }

    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 65;
    public static final int RETIREMENT_AGE = 60;

    public static final LocalTime WORK_START_TIME = LocalTime.of(9, 0);
    public static final LocalTime WORK_END_TIME = LocalTime.of(17, 0);
    public static final int LATE_GRACE_MINUTES = 15;
    public static final double STANDARD_WORK_HOURS = 8.0;

    public static final int ANNUAL_LEAVE_DAYS = 14;
    public static final int CASUAL_LEAVE_DAYS = 10;
    public static final int MEDICAL_LEAVE_DAYS = 10;
    public static final int EMERGENCY_LEAVE_DAYS = 5;

    public static final int MAX_LOGIN_ATTEMPTS = 3;

    public static final double TAX_THRESHOLD = 100000.0;
    public static final double TAX_RATE_ABOVE_THRESHOLD = 0.10;
    public static final double TAX_RATE_BELOW_THRESHOLD = 0.02;

    public static final double TOP_PERFORMER_SCORE = 85.0;
    public static final double PROMOTION_SCORE_THRESHOLD = 80.0;
    public static final int PROMOTION_MIN_EXPERIENCE_YEARS = 2;

    public static final String DATA_DIR = "data";
    public static final String EMPLOYEES_FILE = DATA_DIR + "/employees.dat";
    public static final String BACKUP_DIR = DATA_DIR + "/backups";
    public static final String ACTIVITY_LOG_FILE = DATA_DIR + "/activity_log.txt";
    public static final String EXPORT_DIR = DATA_DIR + "/exports";

    /** Fixed public holidays used by the attendance/working-day calculator. */
    public static final List<MonthDay> HOLIDAYS = List.of(
            MonthDay.of(1, 1),   // New Year's Day
            MonthDay.of(3, 23),  // Pakistan Day
            MonthDay.of(5, 1),   // Labour Day
            MonthDay.of(8, 14),  // Independence Day
            MonthDay.of(11, 9),  // Iqbal Day
            MonthDay.of(12, 25)  // Quaid-e-Azam Day
    );
}

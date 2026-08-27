package com.ems.model;

import com.ems.util.Constants;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/** A single day's check-in/check-out record for one employee. */
public class Attendance implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    public Attendance(LocalDate date, LocalTime checkInTime) {
        this.date = date;
        this.checkInTime = checkInTime;
    }

    public void checkOut(LocalTime time) {
        this.checkOutTime = time;
    }

    public boolean isLate() {
        return checkInTime != null
                && checkInTime.isAfter(Constants.WORK_START_TIME.plusMinutes(Constants.LATE_GRACE_MINUTES));
    }

    public double getWorkingHours() {
        if (checkInTime == null || checkOutTime == null) {
            return 0.0;
        }
        Duration d = Duration.between(checkInTime, checkOutTime);
        return Math.round((d.toMinutes() / 60.0) * 100.0) / 100.0;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    @Override
    public String toString() {
        return date + " | In: " + (checkInTime == null ? "--" : checkInTime)
                + " | Out: " + (checkOutTime == null ? "--" : checkOutTime)
                + " | Hours: " + getWorkingHours()
                + (isLate() ? " | LATE" : "");
    }
}

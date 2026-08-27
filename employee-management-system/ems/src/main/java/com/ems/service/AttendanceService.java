package com.ems.service;

import com.ems.model.Attendance;
import com.ems.model.Employee;

import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin-facing attendance operations. Day-to-day check-in/check-out
 * for a single employee lives on {@link Employee} itself; this class
 * handles the roster-wide views (monthly report, late-arrival list)
 * that only make sense from an admin's point of view.
 */
public class AttendanceService {

    public Attendance checkIn(Employee employee) {
        return employee.markAttendance(LocalTime.now());
    }

    public void checkOut(Employee employee) {
        employee.markCheckOut(LocalTime.now());
    }

    public List<Attendance> getMonthlyRecords(Employee employee, YearMonth month) {
        return employee.viewAttendance().stream()
                .filter(a -> YearMonth.from(a.getDate()).equals(month))
                .collect(Collectors.toList());
    }

    public String getMonthlyReport(Employee employee, YearMonth month) {
        List<Attendance> records = getMonthlyRecords(employee, month);
        long lateCount = records.stream().filter(Attendance::isLate).count();
        double totalHours = records.stream().mapToDouble(Attendance::getWorkingHours).sum();
        StringBuilder sb = new StringBuilder();
        sb.append("Monthly Attendance Report - ").append(employee.getFullName())
                .append(" (").append(month).append(")\n");
        for (Attendance a : records) {
            sb.append("  ").append(a).append('\n');
        }
        sb.append(String.format("  Days present: %d | Late days: %d | Total hours: %.1f | Attendance: %.1f%%",
                records.size(), lateCount, totalHours, employee.getAttendancePercentage()));
        return sb.toString();
    }

    public List<Employee> getLateArrivalsToday(List<Employee> roster) {
        return roster.stream()
                .filter(e -> !e.viewAttendance().isEmpty())
                .filter(e -> e.viewAttendance().get(e.viewAttendance().size() - 1).isLate())
                .collect(Collectors.toList());
    }
}

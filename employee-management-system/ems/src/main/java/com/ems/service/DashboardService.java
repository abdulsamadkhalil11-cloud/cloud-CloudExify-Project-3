package com.ems.service;

import com.ems.model.Developer;
import com.ems.model.Employee;
import com.ems.model.HR;
import com.ems.model.Manager;
import com.ems.model.enums.EmploymentStatus;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Builds the dashboard summary: headcounts, expense, and highlight employees. */
public class DashboardService {

    public String buildDashboard(List<Employee> roster) {
        if (roster.isEmpty()) {
            return "Dashboard\n  No employees on file yet.";
        }
        long managers = roster.stream().filter(e -> e instanceof Manager).count();
        long developers = roster.stream().filter(e -> e instanceof Developer).count();
        long hrStaff = roster.stream().filter(e -> e instanceof HR).count();
        long departments = roster.stream().map(Employee::getDepartment).distinct().count();
        long active = roster.stream().filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE).count();
        long inactive = roster.size() - active;
        double totalExpense = roster.stream().mapToDouble(Employee::calculateSalary).sum();
        double avgSalary = totalExpense / roster.size();
        double avgAttendance = roster.stream().mapToDouble(Employee::getAttendancePercentage).average().orElse(0);
        Employee highestPerformer = roster.stream()
                .max(Comparator.comparingDouble(Employee::getPerformanceScore)).orElse(null);
        Employee latest = roster.stream()
                .max(Comparator.comparing(Employee::getJoiningDate)).orElse(null);

        return String.format(
                "===== DASHBOARD =====%n"
                        + "Total Employees:      %d%n"
                        + "  Managers:            %d%n"
                        + "  Developers:          %d%n"
                        + "  HR Staff:            %d%n"
                        + "Departments:           %d%n"
                        + "Active Employees:      %d%n"
                        + "Inactive Employees:    %d%n"
                        + "Total Salary Expense:  %.2f%n"
                        + "Average Salary:        %.2f%n"
                        + "Average Attendance:    %.1f%%%n"
                        + "Highest Performer:     %s%n"
                        + "Latest Employee:       %s",
                roster.size(), managers, developers, hrStaff, departments, active, inactive,
                totalExpense, avgSalary, avgAttendance,
                highestPerformer == null ? "-" : highestPerformer.getFullName(),
                latest == null ? "-" : latest.getFullName());
    }

    public java.util.Map<String, Long> departmentBreakdown(List<Employee> roster) {
        return roster.stream().collect(Collectors.groupingBy(
                e -> e.getDepartment() == null ? "UNASSIGNED" : e.getDepartment().name(),
                Collectors.counting()));
    }
}

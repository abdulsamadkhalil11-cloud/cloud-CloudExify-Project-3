package com.ems.service;

import com.ems.exceptions.FileOperationException;
import com.ems.model.Employee;
import com.ems.model.LeaveRequest;
import com.ems.model.enums.Department;
import com.ems.model.enums.LeaveStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Every named report from the spec, plus CSV export and plain-text "printable" export. */
public class ReportService {

    public String employeeReport(Employee employee) {
        return employee.generateEmployeeReport();
    }

    public String departmentReport(List<Employee> roster, Department department) {
        List<Employee> inDept = roster.stream().filter(e -> e.getDepartment() == department).collect(Collectors.toList());
        double total = inDept.stream().mapToDouble(Employee::calculateSalary).sum();
        StringBuilder sb = new StringBuilder("Department Report - " + department + "\n");
        inDept.forEach(e -> sb.append("  ").append(e).append('\n'));
        sb.append(String.format("Headcount: %d | Total Salary: %.2f", inDept.size(), total));
        return sb.toString();
    }

    public String salaryReport(List<Employee> roster) {
        double total = roster.stream().mapToDouble(Employee::calculateSalary).sum();
        double avg = roster.isEmpty() ? 0 : total / roster.size();
        StringBuilder sb = new StringBuilder("Salary Report\n");
        roster.stream()
                .sorted(Comparator.comparingDouble(Employee::calculateSalary).reversed())
                .forEach(e -> sb.append(String.format("  %-10s %-20s %10.2f%n", e.getEmployeeId(), e.getFullName(), e.calculateSalary())));
        sb.append(String.format("Total: %.2f | Average: %.2f", total, avg));
        return sb.toString();
    }

    public String attendanceReport(List<Employee> roster) {
        StringBuilder sb = new StringBuilder("Attendance Report\n");
        roster.forEach(e -> sb.append(String.format("  %-10s %-20s %.1f%%%n", e.getEmployeeId(), e.getFullName(), e.getAttendancePercentage())));
        return sb.toString();
    }

    public String performanceReport(List<Employee> roster) {
        StringBuilder sb = new StringBuilder("Performance Report\n");
        roster.stream()
                .sorted(Comparator.comparingDouble(Employee::getPerformanceScore).reversed())
                .forEach(e -> sb.append(String.format("  %-10s %-20s %.1f%n", e.getEmployeeId(), e.getFullName(), e.getPerformanceScore())));
        return sb.toString();
    }

    public String leaveReport(List<LeaveRequest> requests) {
        long approved = requests.stream().filter(r -> r.getStatus() == LeaveStatus.APPROVED).count();
        long rejected = requests.stream().filter(r -> r.getStatus() == LeaveStatus.REJECTED).count();
        long pending = requests.stream().filter(r -> r.getStatus() == LeaveStatus.PENDING).count();
        StringBuilder sb = new StringBuilder("Leave Report\n");
        requests.forEach(r -> sb.append("  ").append(r).append('\n'));
        sb.append(String.format("Approved: %d | Rejected: %d | Pending: %d", approved, rejected, pending));
        return sb.toString();
    }

    public String topEmployeesReport(List<Employee> roster, int n) {
        return namedTopList("Top " + n + " Employees (by performance)", roster, n, true,
                Comparator.comparingDouble(Employee::getPerformanceScore));
    }

    public String lowestPerformanceReport(List<Employee> roster, int n) {
        return namedTopList("Lowest " + n + " Performers", roster, n, false,
                Comparator.comparingDouble(Employee::getPerformanceScore));
    }

    public String highestSalaryReport(List<Employee> roster, int n) {
        return namedTopList("Highest " + n + " Salaries", roster, n, true,
                Comparator.comparingDouble(Employee::calculateSalary));
    }

    private String namedTopList(String title, List<Employee> roster, int n, boolean descending,
                                 Comparator<Employee> comparator) {
        Comparator<Employee> ordered = descending ? comparator.reversed() : comparator;
        StringBuilder sb = new StringBuilder(title + "\n");
        roster.stream().sorted(ordered).limit(n)
                .forEach(e -> sb.append("  ").append(e.generateEmployeeReport()).append('\n'));
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Export
    // ---------------------------------------------------------------

    public String exportToCsv(List<Employee> roster, String filepath) throws FileOperationException {
        try {
            Path path = Paths.get(filepath);
            Files.createDirectories(path.getParent());
            StringBuilder sb = new StringBuilder(
                    "EmployeeID,FullName,Role,Department,Designation,Salary,Attendance,Performance,Status\n");
            for (Employee e : roster) {
                sb.append(String.join(",",
                        e.getEmployeeId(), csvSafe(e.getFullName()), e.getClass().getSimpleName(),
                        String.valueOf(e.getDepartment()), csvSafe(e.getDesignation()),
                        String.valueOf(e.calculateSalary()), String.valueOf(e.getAttendancePercentage()),
                        String.valueOf(e.getPerformanceScore()), String.valueOf(e.getEmploymentStatus())))
                        .append('\n');
            }
            Files.writeString(path, sb.toString());
            return filepath;
        } catch (IOException e) {
            throw new FileOperationException("CSV export failed", e);
        }
    }

    private String csvSafe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }

    /** "Printable Reports" bonus feature: write any report string out to a text file. */
    public String exportReportToTextFile(String reportContent, String filepath) throws FileOperationException {
        try {
            Path path = Paths.get(filepath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, reportContent);
            return filepath;
        } catch (IOException e) {
            throw new FileOperationException("Report export failed", e);
        }
    }
}

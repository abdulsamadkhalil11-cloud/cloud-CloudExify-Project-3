package com.ems.app;

import com.ems.exceptions.FileOperationException;
import com.ems.model.Admin;
import com.ems.model.SystemData;
import com.ems.service.*;
import com.ems.util.Constants;

/**
 * Loads saved data if it exists, otherwise seeds a sample roster.
 * Shared by the console app and the JavaFX app so the two entry
 * points never drift into two different startup behaviours.
 */
public final class DataBootstrap {

    private DataBootstrap() {
    }

    public static Admin bootstrap(FileStorageService fileStorageService, EmployeeService employeeService,
                                   LeaveService leaveService, SalaryService salaryService,
                                   PerformanceService performanceService, AuditLogService auditLogService) {
        if (fileStorageService.exists(Constants.EMPLOYEES_FILE)) {
            try {
                SystemData data = fileStorageService.load(Constants.EMPLOYEES_FILE);
                employeeService.setAll(data.getEmployees());
                leaveService.setAll(data.getLeaveRequests());
                salaryService.setAll(data.getSalarySlips());
                performanceService.setAll(data.getPerformanceReviews());
                return data.getAdmin();
            } catch (FileOperationException e) {
                System.out.println("Could not load saved data (" + e.getMessage() + "); seeding sample data instead.");
            }
        }
        try {
            Admin admin = SampleDataSeeder.seed(employeeService, leaveService, performanceService);
            auditLogService.logQuiet("System started with freshly seeded sample data");
            return admin;
        } catch (Exception e) {
            System.out.println("Could not seed sample data: " + e.getMessage());
            return new Admin("admin", "Admin@123");
        }
    }
}

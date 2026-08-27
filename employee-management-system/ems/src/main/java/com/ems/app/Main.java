package com.ems.app;

import com.ems.exceptions.FileOperationException;
import com.ems.exceptions.InvalidCredentialsException;
import com.ems.model.Admin;
import com.ems.model.Employee;
import com.ems.model.SystemData;
import com.ems.service.*;
import com.ems.util.Constants;
import com.ems.util.InputHelper;

import java.util.Scanner;

/**
 * Employee Management System - console entry point.
 * Run with: java -cp out com.ems.app.Main
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InputHelper input = new InputHelper(scanner);

        EmployeeService employeeService = new EmployeeService();
        AttendanceService attendanceService = new AttendanceService();
        LeaveService leaveService = new LeaveService();
        SalaryService salaryService = new SalaryService();
        PerformanceService performanceService = new PerformanceService();
        ReportService reportService = new ReportService();
        DashboardService dashboardService = new DashboardService();
        FileStorageService fileStorageService = new FileStorageService();
        AuditLogService auditLogService = new AuditLogService(Constants.ACTIVITY_LOG_FILE);
        AuthService authService = new AuthService();

        System.out.println("=================================================");
        System.out.println("   EMPLOYEE MANAGEMENT SYSTEM");
        System.out.println("=================================================");

        Admin admin = DataBootstrap.bootstrap(fileStorageService, employeeService, leaveService,
                salaryService, performanceService, auditLogService);

        boolean appRunning = true;
        while (appRunning) {
            try {
                System.out.println("\n1) Admin Login   2) Employee Login   3) Exit");
                int choice = input.readIntInRange("Choice: ", 1, 3);
                switch (choice) {
                    case 1:
                        handleAdminLogin(admin, input, employeeService, attendanceService, leaveService,
                                salaryService, performanceService, reportService, dashboardService,
                                fileStorageService, auditLogService);
                        break;
                    case 2:
                        handleEmployeeLogin(employeeService, leaveService, salaryService, authService, input);
                        break;
                    default:
                        appRunning = false;
                }
            } catch (java.util.NoSuchElementException e) {
                System.out.println("\nInput stream ended - exiting.");
                appRunning = false;
            }
        }

        System.out.println("\nSession ended. Thanks for using the Employee Management System.");
        scanner.close();
    }

    private static void handleAdminLogin(Admin admin, InputHelper input, EmployeeService employeeService,
                                          AttendanceService attendanceService, LeaveService leaveService,
                                          SalaryService salaryService, PerformanceService performanceService,
                                          ReportService reportService, DashboardService dashboardService,
                                          FileStorageService fileStorageService, AuditLogService auditLogService) {
        for (int attempt = 1; attempt <= Constants.MAX_LOGIN_ATTEMPTS; attempt++) {
            String username = input.readNonEmptyString("Admin username: ");
            String password = input.readNonEmptyString("Password: ");
            try {
                admin.login(username, password);
                System.out.println("Welcome, " + admin.getUsername() + ".");
                new AdminMenu(employeeService, attendanceService, leaveService, salaryService,
                        performanceService, reportService, dashboardService, fileStorageService,
                        auditLogService, admin, input).run();
                return;
            } catch (InvalidCredentialsException e) {
                System.out.println("  Login failed (" + attempt + "/" + Constants.MAX_LOGIN_ATTEMPTS + "): " + e.getMessage());
            }
        }
        System.out.println("Too many failed attempts. Returning to main menu.");
    }

    private static void handleEmployeeLogin(EmployeeService employeeService, LeaveService leaveService,
                                             SalaryService salaryService, AuthService authService, InputHelper input) {
        for (int attempt = 1; attempt <= Constants.MAX_LOGIN_ATTEMPTS; attempt++) {
            String id = input.readNonEmptyString("Employee ID: ");
            String password = input.readNonEmptyString("Password: ");
            try {
                Employee employee = authService.authenticateEmployee(employeeService.getAll(), id, password);
                System.out.println("Welcome, " + employee.getFullName() + ".");
                new EmployeeMenu(employee, leaveService, salaryService, authService, input).run();
                return;
            } catch (InvalidCredentialsException e) {
                if (attempt == Constants.MAX_LOGIN_ATTEMPTS) {
                    System.out.println("  Login failed (" + attempt + "/" + Constants.MAX_LOGIN_ATTEMPTS + "). "
                            + "Forgot your password? Ask HR/Admin to reset it, or use CNIC verification below.");
                    offerForgotPassword(employeeService, authService, input, id);
                } else {
                    System.out.println("  Login failed (" + attempt + "/" + Constants.MAX_LOGIN_ATTEMPTS + ").");
                }
            }
        }
    }

    private static void offerForgotPassword(EmployeeService employeeService, AuthService authService,
                                             InputHelper input, String employeeId) {
        if (!input.readYesNo("Try forgot-password recovery now?")) {
            return;
        }
        try {
            Employee employee = employeeService.getById(employeeId);
            String cnic = input.readCnic("Confirm your CNIC on file");
            String tempPassword = authService.forgotPassword(employee, cnic);
            System.out.println("  Identity verified. Temporary password: " + tempPassword
                    + " (log in and change it right away).");
        } catch (Exception e) {
            System.out.println("  Could not recover the account: " + e.getMessage());
        }
    }
}

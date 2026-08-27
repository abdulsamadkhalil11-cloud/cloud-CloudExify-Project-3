package com.ems.app;

import com.ems.exceptions.EMSException;
import com.ems.exceptions.InvalidCredentialsException;
import com.ems.model.Employee;
import com.ems.model.LeaveRequest;
import com.ems.model.enums.LeaveType;
import com.ems.service.AuthService;
import com.ems.service.LeaveService;
import com.ems.service.SalaryService;
import com.ems.util.InputHelper;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * The Employee-facing console: a smaller, self-service menu. An
 * employee can manage their own attendance, leave, salary slip and
 * password, but has no access to the roster-wide Admin actions -
 * that split follows the spec's separate "Admin Login" / "Employee
 * Login" sections.
 */
public class EmployeeMenu {

    private final Employee employee;
    private final LeaveService leaveService;
    private final SalaryService salaryService;
    private final AuthService authService;
    private final InputHelper input;

    public EmployeeMenu(Employee employee, LeaveService leaveService, SalaryService salaryService,
                         AuthService authService, InputHelper input) {
        this.employee = employee;
        this.leaveService = leaveService;
        this.salaryService = salaryService;
        this.authService = authService;
        this.input = input;
    }

    public void run() {
        if (employee.isBirthdayToday()) {
            System.out.println("\n*** Happy Birthday, " + employee.getFullName() + "! ***");
        }
        if (!employee.getNotifications().isEmpty()) {
            System.out.println("You have " + employee.getNotifications().size() + " notification(s):");
            employee.getNotifications().forEach(n -> System.out.println("  - " + n));
        }

        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.readIntInRange("Choice: ", 0, 8);
                switch (choice) {
                    case 1: employee.displayInformation(); break;
                    case 2: checkInOut(); break;
                    case 3: employee.viewAttendance().forEach(a -> System.out.println("  " + a)); break;
                    case 4: applyLeave(); break;
                    case 5: viewLeaveInfo(); break;
                    case 6: requestSalarySlip(); break;
                    case 7: changePassword(); break;
                    case 8: updateContactInfo(); break;
                    case 0:
                        employee.logout();
                        running = false;
                        System.out.println("Logged out.");
                        break;
                }
            } catch (java.util.NoSuchElementException e) {
                System.out.println("\n  Input stream ended - exiting.");
                running = false;
            } catch (EMSException e) {
                System.out.println("  [Error] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Unexpected error] " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n===== EMPLOYEE MENU (" + employee.getFullName() + ") =====");
        System.out.println("1  View my profile");
        System.out.println("2  Check in / Check out");
        System.out.println("3  View my attendance");
        System.out.println("4  Apply for leave");
        System.out.println("5  My leave history / remaining balance");
        System.out.println("6  View a salary slip");
        System.out.println("7  Change password");
        System.out.println("8  Update contact info");
        System.out.println("0  Logout");
    }

    private void checkInOut() {
        if (input.readYesNo("Check in now (n = check out instead)?")) {
            employee.markAttendance(java.time.LocalTime.now());
            System.out.println("  Checked in.");
        } else {
            employee.markCheckOut(java.time.LocalTime.now());
            System.out.println("  Checked out.");
        }
    }

    private void applyLeave() {
        System.out.println("Leave type: 1) Medical 2) Casual 3) Annual 4) Emergency");
        LeaveType type = LeaveType.values()[input.readIntInRange("Type: ", 1, 4) - 1];
        LocalDate start = input.readDate("Start date");
        LocalDate end = input.readDate("End date");
        String reason = input.readNonEmptyString("Reason: ");
        LeaveRequest r = leaveService.applyLeave(employee.getEmployeeId(), type, start, end, reason);
        System.out.println("  Submitted: " + r);
    }

    private void viewLeaveInfo() {
        leaveService.getHistory(employee.getEmployeeId()).forEach(r -> System.out.println("  " + r));
        for (LeaveType type : LeaveType.values()) {
            System.out.println("  " + type + " remaining: " + leaveService.getRemainingLeaves(employee.getEmployeeId(), type) + " days");
        }
    }

    private void requestSalarySlip() {
        YearMonth month = input.readMonth("Month");
        System.out.println(salaryService.generateSalarySlip(employee, month, 0.0, 0.0));
    }

    private void changePassword() throws InvalidCredentialsException {
        String oldPassword = input.readNonEmptyString("Current password: ");
        String newPassword = input.readStrongPassword("New password");
        authService.changePassword(employee, oldPassword, newPassword);
        System.out.println("  Password updated.");
    }

    private void updateContactInfo() throws com.ems.exceptions.InvalidEmailException {
        String phone = input.readPhone("New phone: ");
        String email = input.readEmail("New email: ");
        String address = input.readNonEmptyString("New address: ");
        employee.updateProfile(phone, email, address, employee.getDesignation());
        System.out.println("  Updated.");
    }
}

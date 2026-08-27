package com.ems.app;

import com.ems.exceptions.*;
import com.ems.model.*;
import com.ems.model.enums.*;
import com.ems.service.*;
import com.ems.util.Constants;
import com.ems.util.InputHelper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * The Admin-facing console: exactly the 15-item menu from the spec,
 * each backed by real service calls. Every action that can fail is
 * wrapped so the specific EMSException subtype is caught and shown
 * as a friendly message instead of crashing the app.
 */
public class AdminMenu {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final SalaryService salaryService;
    private final PerformanceService performanceService;
    private final ReportService reportService;
    private final DashboardService dashboardService;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;
    private final Admin admin;
    private final InputHelper input;

    public AdminMenu(EmployeeService employeeService, AttendanceService attendanceService,
                      LeaveService leaveService, SalaryService salaryService,
                      PerformanceService performanceService, ReportService reportService,
                      DashboardService dashboardService, FileStorageService fileStorageService,
                      AuditLogService auditLogService, Admin admin, InputHelper input) {
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
        this.salaryService = salaryService;
        this.performanceService = performanceService;
        this.reportService = reportService;
        this.dashboardService = dashboardService;
        this.fileStorageService = fileStorageService;
        this.auditLogService = auditLogService;
        this.admin = admin;
        this.input = input;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.readInt("Choice: ");
                switch (choice) {
                    case 1: addEmployee(); break;
                    case 2: removeEmployee(); break;
                    case 3: updateEmployee(); break;
                    case 4: searchEmployee(); break;
                    case 5: viewEmployee(); break;
                    case 6: attendanceMenu(); break;
                    case 7: salaryMenu(); break;
                    case 8: leaveMenu(); break;
                    case 9: reportsMenu(); break;
                    case 10: showDashboard(); break;
                    case 11: saveData(); break;
                    case 12: loadData(); break;
                    case 13: backupData(); break;
                    case 14: restoreData(); break;
                    case 15: running = confirmExit(); break;
                    case 0: admin.logout(); running = false; break;
                    default: System.out.println("Invalid choice - pick a number from the menu (1-15).");
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
        System.out.println("\n===== ADMIN MENU (" + admin.getUsername() + ") =====");
        System.out.println(" 1  Add Employee");
        System.out.println(" 2  Remove Employee");
        System.out.println(" 3  Update Employee");
        System.out.println(" 4  Search Employee");
        System.out.println(" 5  View Employee");
        System.out.println(" 6  Attendance");
        System.out.println(" 7  Salary");
        System.out.println(" 8  Leave");
        System.out.println(" 9  Reports");
        System.out.println("10  Dashboard");
        System.out.println("11  Save Data");
        System.out.println("12  Load Data");
        System.out.println("13  Backup");
        System.out.println("14  Restore");
        System.out.println("15  Exit");
        System.out.println(" 0  Logout");
    }

    // ---------------------------------------------------------------
    // 1. Add Employee
    // ---------------------------------------------------------------
    private void addEmployee() throws EMSException {
        System.out.println("\n-- Add Employee --");
        System.out.println("Role: 1) Manager  2) Developer  3) HR");
        int role = input.readIntInRange("Role: ", 1, 3);

        String fullName = input.readNonEmptyString("Full name: ");
        int age = input.readIntInRange("Age (18-65): ", Constants.MIN_AGE, Constants.MAX_AGE);
        System.out.println("Gender: 1) Male  2) Female  3) Other");
        Gender gender = Gender.values()[input.readIntInRange("Gender: ", 1, 3) - 1];
        String cnic = input.readCnic("CNIC");
        String phone = input.readPhone("Phone: ");
        String email = input.readEmail("Email: ");
        String address = input.readNonEmptyString("Address: ");
        Department department = pickDepartment();
        String designation = input.readNonEmptyString("Designation: ");
        double salary = input.readNonNegativeDouble("Base salary: ");
        LocalDate joiningDate = input.readDate("Joining date");
        LocalDate dob = input.readDate("Date of birth");
        String username = input.readNonEmptyString("Username: ");
        String password = input.readStrongPassword("Password");

        Employee employee;
        switch (role) {
            case 1: {
                int teamSize = input.readIntInRange("Team size: ", 0, 500);
                double budget = input.readNonNegativeDouble("Department budget: ");
                employee = new Manager(employeeService.generateNextId("MGR"), fullName, age, gender, cnic,
                        phone, email, address, department, designation, salary, joiningDate, dob,
                        username, password, teamSize, budget);
                break;
            }
            case 2: {
                String language = input.readNonEmptyString("Programming language: ");
                String framework = input.readNonEmptyString("Framework: ");
                String github = input.readNonEmptyString("GitHub username: ");
                employee = new Developer(employeeService.generateNextId("DEV"), fullName, age, gender, cnic,
                        phone, email, address, department, designation, salary, joiningDate, dob,
                        username, password, language, framework, github);
                break;
            }
            default: {
                employee = new HR(employeeService.generateNextId("HR"), fullName, age, gender, cnic,
                        phone, email, address, department, designation, salary, joiningDate, dob,
                        username, password);
            }
        }
        employeeService.addEmployee(employee);
        auditLogService.logQuiet("Added employee " + employee.getEmployeeId() + " (" + fullName + ")");
        System.out.println("  Added. New employee ID: " + employee.getEmployeeId());
    }

    private Department pickDepartment() {
        Department[] values = Department.values();
        System.out.println("Department:");
        for (int i = 0; i < values.length; i++) {
            System.out.println("  " + (i + 1) + ") " + values[i]);
        }
        return values[input.readIntInRange("Department: ", 1, values.length) - 1];
    }

    // ---------------------------------------------------------------
    // 2. Remove Employee
    // ---------------------------------------------------------------
    private void removeEmployee() throws EmployeeNotFoundException {
        String id = input.readNonEmptyString("Employee ID to remove: ");
        Employee removed = employeeService.removeEmployee(id);
        auditLogService.logQuiet("Removed employee " + id);
        System.out.println("  Removed: " + removed.getFullName());
    }

    // ---------------------------------------------------------------
    // 3. Update Employee
    // ---------------------------------------------------------------
    private void updateEmployee() throws EMSException {
        Employee e = employeeService.getById(input.readNonEmptyString("Employee ID to update: "));
        System.out.println("Editing " + e);
        System.out.println("1) Contact info  2) Salary  3) Employment status  4) Department/Designation"
                + "  5) Profile picture path  6) Add note");
        int choice = input.readIntInRange("Field group: ", 1, 6);
        switch (choice) {
            case 1: {
                String phone = input.readPhone("New phone: ");
                String email = input.readEmail("New email: ");
                String address = input.readNonEmptyString("New address: ");
                e.updateProfile(phone, email, address, e.getDesignation());
                break;
            }
            case 2: {
                e.setSalary(input.readNonNegativeDouble("New base salary: "));
                break;
            }
            case 3: {
                EmploymentStatus[] values = EmploymentStatus.values();
                for (int i = 0; i < values.length; i++) System.out.println("  " + (i + 1) + ") " + values[i]);
                e.setEmploymentStatus(values[input.readIntInRange("Status: ", 1, values.length) - 1]);
                break;
            }
            case 4: {
                e.setDepartment(pickDepartment());
                e.setDesignation(input.readNonEmptyString("New designation: "));
                break;
            }
            case 5: {
                e.updateProfile(input.readNonEmptyString("Profile picture path: "));
                break;
            }
            default: {
                e.addNote(input.readNonEmptyString("Note: "));
            }
        }
        auditLogService.logQuiet("Updated employee " + e.getEmployeeId());
        System.out.println("  Updated.");
    }

    // ---------------------------------------------------------------
    // 4. Search Employee
    // ---------------------------------------------------------------
    private void searchEmployee() {
        System.out.println("\n-- Search --");
        System.out.println("1) ID  2) Name  3) Department  4) Salary range  5) Phone  6) Email"
                + "  7) Designation  8) Status  9) Programming language  10) Manager");
        int choice = input.readIntInRange("Search by: ", 1, 10);
        List<Employee> results;
        switch (choice) {
            case 1: results = employeeService.searchByEmployeeId(input.readNonEmptyString("ID: ")); break;
            case 2: results = employeeService.searchByName(input.readNonEmptyString("Name contains: ")); break;
            case 3: results = employeeService.searchByDepartment(pickDepartment()); break;
            case 4: {
                double min = input.readNonNegativeDouble("Min salary: ");
                double max = input.readNonNegativeDouble("Max salary: ");
                results = employeeService.searchBySalaryRange(min, max);
                break;
            }
            case 5: results = employeeService.searchByPhone(input.readNonEmptyString("Phone contains: ")); break;
            case 6: results = employeeService.searchByEmail(input.readNonEmptyString("Email: ")); break;
            case 7: results = employeeService.searchByDesignation(input.readNonEmptyString("Designation contains: ")); break;
            case 8: results = employeeService.searchByStatus(input.readNonEmptyString("Status (ACTIVE/ON_LEAVE/INACTIVE/TERMINATED): ")); break;
            case 9: results = employeeService.searchByProgrammingLanguage(input.readNonEmptyString("Language contains: ")); break;
            default: results = employeeService.searchByManager(input.readNonEmptyString("Manager ID: "));
        }
        printResults(results);
    }

    private void printResults(List<Employee> results) {
        if (results.isEmpty()) {
            System.out.println("  No matches.");
            return;
        }
        results.forEach(e -> System.out.println("  " + e));
    }

    // ---------------------------------------------------------------
    // 5. View Employee
    // ---------------------------------------------------------------
    private void viewEmployee() throws EmployeeNotFoundException {
        System.out.println("1) View one employee  2) View all employees");
        if (input.readIntInRange("Choice: ", 1, 2) == 1) {
            Employee e = employeeService.getById(input.readNonEmptyString("Employee ID: "));
            e.displayInformation();
            if (!e.getAchievements().isEmpty()) System.out.println("  Achievements: " + e.getAchievements());
            if (!e.getRewards().isEmpty()) System.out.println("  Rewards: " + e.getRewards());
            if (!e.getCertificates().isEmpty()) System.out.println("  Certificates: " + e.getCertificates());
            if (!e.getNotes().isEmpty()) System.out.println("  Notes: " + e.getNotes());
            System.out.println("  Years to retirement: " + e.getYearsToRetirement());
            if (e.isBirthdayToday()) System.out.println("  ** Birthday today! **");
        } else {
            employeeService.getAll().forEach(Employee::displayInformation);
        }
    }

    // ---------------------------------------------------------------
    // 6. Attendance
    // ---------------------------------------------------------------
    private void attendanceMenu() throws EMSException {
        System.out.println("\n-- Attendance --");
        System.out.println("1) Check in  2) Check out  3) View attendance  4) Monthly report  5) Late arrivals today");
        int choice = input.readIntInRange("Choice: ", 1, 5);
        if (choice == 5) {
            printResults(attendanceService.getLateArrivalsToday(employeeService.getAll()));
            return;
        }
        Employee e = employeeService.getById(input.readNonEmptyString("Employee ID: "));
        switch (choice) {
            case 1:
                attendanceService.checkIn(e);
                System.out.println("  Checked in at " + java.time.LocalTime.now().withNano(0));
                break;
            case 2:
                attendanceService.checkOut(e);
                System.out.println("  Checked out at " + java.time.LocalTime.now().withNano(0));
                break;
            case 3:
                e.viewAttendance().forEach(a -> System.out.println("  " + a));
                break;
            default:
                System.out.println(attendanceService.getMonthlyReport(e, input.readMonth("Month")));
        }
    }

    // ---------------------------------------------------------------
    // 7. Salary
    // ---------------------------------------------------------------
    private void salaryMenu() throws EMSException {
        System.out.println("\n-- Salary --");
        System.out.println("1) Generate salary slip  2) Run monthly payroll  3) Salary calculator  4) Total monthly expense");
        int choice = input.readIntInRange("Choice: ", 1, 4);
        switch (choice) {
            case 1: {
                Employee e = employeeService.getById(input.readNonEmptyString("Employee ID: "));
                YearMonth month = input.readMonth("Month");
                double allowance = input.readNonNegativeDouble("Allowance: ");
                double deductions = input.readNonNegativeDouble("Deductions: ");
                SalarySlip slip = salaryService.generateSalarySlip(e, month, allowance, deductions);
                System.out.println(slip);
                break;
            }
            case 2: {
                List<SalarySlip> batch = salaryService.runMonthlyPayroll(employeeService.getAll(), input.readMonth("Payroll month"));
                System.out.println("  Generated " + batch.size() + " salary slips.");
                break;
            }
            case 3: {
                double basic = input.readNonNegativeDouble("Basic: ");
                double bonus = input.readNonNegativeDouble("Bonus: ");
                double allowance = input.readNonNegativeDouble("Allowance: ");
                double deductions = input.readNonNegativeDouble("Deductions: ");
                System.out.printf("  Estimated net salary: %.2f%n",
                        salaryService.estimateNetSalary(basic, bonus, allowance, deductions));
                break;
            }
            default:
                System.out.printf("  Total monthly expense: %.2f%n",
                        salaryService.getTotalMonthlyExpense(employeeService.getAll()));
        }
    }

    // ---------------------------------------------------------------
    // 8. Leave
    // ---------------------------------------------------------------
    private void leaveMenu() throws EmployeeNotFoundException {
        System.out.println("\n-- Leave --");
        System.out.println("1) Apply leave  2) Approve/Reject pending  3) Leave history  4) Remaining leaves");
        int choice = input.readIntInRange("Choice: ", 1, 4);
        switch (choice) {
            case 1: {
                Employee e = employeeService.getById(input.readNonEmptyString("Employee ID: "));
                LeaveType type = pickLeaveType();
                LocalDate start = input.readDate("Start date");
                LocalDate end = input.readDate("End date");
                String reason = input.readNonEmptyString("Reason: ");
                LeaveRequest r = leaveService.applyLeave(e.getEmployeeId(), type, start, end, reason);
                System.out.println("  Applied: " + r);
                break;
            }
            case 2: {
                List<LeaveRequest> pending = leaveService.getPendingRequests();
                if (pending.isEmpty()) {
                    System.out.println("  No pending requests.");
                    return;
                }
                pending.forEach(r -> System.out.println("  " + r));
                String id = input.readNonEmptyString("Request ID to decide: ");
                LeaveRequest match = pending.stream().filter(r -> r.getRequestId().equalsIgnoreCase(id))
                        .findFirst().orElse(null);
                if (match == null) {
                    System.out.println("  No such pending request.");
                    return;
                }
                if (input.readYesNo("Approve?")) {
                    leaveService.approve(match, admin.getUsername());
                } else {
                    leaveService.reject(match, admin.getUsername());
                }
                System.out.println("  Decision recorded.");
                break;
            }
            case 3: {
                String id = input.readNonEmptyString("Employee ID: ");
                leaveService.getHistory(id).forEach(r -> System.out.println("  " + r));
                break;
            }
            default: {
                Employee e = employeeService.getById(input.readNonEmptyString("Employee ID: "));
                LeaveType type = pickLeaveType();
                System.out.println("  Remaining " + type + " leave: " + leaveService.getRemainingLeaves(e.getEmployeeId(), type) + " days");
            }
        }
    }

    private LeaveType pickLeaveType() {
        LeaveType[] values = LeaveType.values();
        for (int i = 0; i < values.length; i++) System.out.println("  " + (i + 1) + ") " + values[i]);
        return values[input.readIntInRange("Leave type: ", 1, values.length) - 1];
    }

    // ---------------------------------------------------------------
    // 9. Reports
    // ---------------------------------------------------------------
    private void reportsMenu() throws EMSException {
        System.out.println("\n-- Reports --");
        System.out.println("1) Employee  2) Department  3) Salary  4) Attendance  5) Performance  6) Leave"
                + "  7) Top employees  8) Lowest performance  9) Highest salary  10) Export roster to CSV"
                + "  11) Recent activity log");
        int choice = input.readIntInRange("Report: ", 1, 11);
        String output;
        switch (choice) {
            case 1: output = reportService.employeeReport(employeeService.getById(input.readNonEmptyString("Employee ID: "))); break;
            case 2: output = reportService.departmentReport(employeeService.getAll(), pickDepartment()); break;
            case 3: output = reportService.salaryReport(employeeService.getAll()); break;
            case 4: output = reportService.attendanceReport(employeeService.getAll()); break;
            case 5: output = reportService.performanceReport(employeeService.getAll()); break;
            case 6: output = reportService.leaveReport(leaveService.getAll()); break;
            case 7: output = reportService.topEmployeesReport(employeeService.getAll(), input.readIntInRange("How many: ", 1, 50)); break;
            case 8: output = reportService.lowestPerformanceReport(employeeService.getAll(), input.readIntInRange("How many: ", 1, 50)); break;
            case 9: output = reportService.highestSalaryReport(employeeService.getAll(), input.readIntInRange("How many: ", 1, 50)); break;
            case 10: {
                String path = reportService.exportToCsv(employeeService.getAll(), Constants.EXPORT_DIR + "/employees.csv");
                System.out.println("  Exported to " + path);
                return;
            }
            default: {
                auditLogService.getRecent(20).forEach(System.out::println);
                return;
            }
        }
        System.out.println(output);
        if (input.readYesNo("Save this report to a text file?")) {
            String path = reportService.exportReportToTextFile(output, Constants.EXPORT_DIR + "/report_" + System.currentTimeMillis() + ".txt");
            System.out.println("  Saved to " + path);
        }
    }

    // ---------------------------------------------------------------
    // 10. Dashboard
    // ---------------------------------------------------------------
    private void showDashboard() {
        System.out.println(dashboardService.buildDashboard(employeeService.getAll()));
    }

    // ---------------------------------------------------------------
    // 11-14. Save / Load / Backup / Restore
    // ---------------------------------------------------------------
    private void saveData() throws FileOperationException {
        SystemData data = new SystemData();
        data.setEmployees(employeeService.getAll());
        data.setLeaveRequests(leaveService.getAll());
        data.setSalarySlips(salaryService.getAll());
        data.setPerformanceReviews(performanceService.getAll());
        data.setAdmin(admin);
        fileStorageService.save(data, Constants.EMPLOYEES_FILE);
        auditLogService.logQuiet("Saved data (" + data.getEmployees().size() + " employees)");
        System.out.println("  Saved.");
    }

    private void loadData() throws FileOperationException {
        SystemData data = fileStorageService.load(Constants.EMPLOYEES_FILE);
        employeeService.setAll(data.getEmployees());
        leaveService.setAll(data.getLeaveRequests());
        salaryService.setAll(data.getSalarySlips());
        performanceService.setAll(data.getPerformanceReviews());
        reserveIdsFromLoadedData(data.getEmployees());
        System.out.println("  Loaded " + data.getEmployees().size() + " employees.");
    }

    private void reserveIdsFromLoadedData(List<Employee> loaded) {
        for (Employee e : loaded) {
            String[] parts = e.getEmployeeId().split("-");
            if (parts.length == 2) {
                try {
                    com.ems.util.IdGenerator.reserve(parts[0], Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                    // Non-standard ID format (e.g. hand-typed) - nothing to reserve.
                }
            }
        }
    }

    private void backupData() throws FileOperationException {
        String path = fileStorageService.backup(Constants.EMPLOYEES_FILE, Constants.BACKUP_DIR);
        System.out.println("  Backed up to " + path);
    }

    private void restoreData() throws FileOperationException {
        List<String> backups = fileStorageService.listBackups(Constants.BACKUP_DIR);
        if (backups.isEmpty()) {
            System.out.println("  No backups found.");
            return;
        }
        backups.forEach(b -> System.out.println("  " + b));
        String chosen = input.readNonEmptyString("Backup file path to restore: ");
        fileStorageService.restore(chosen, Constants.EMPLOYEES_FILE);
        loadData();
        System.out.println("  Restored.");
    }

    // ---------------------------------------------------------------
    // 15. Exit
    // ---------------------------------------------------------------
    private boolean confirmExit() throws FileOperationException {
        if (input.readYesNo("Save data before exiting?")) {
            saveData();
        }
        System.out.println("Goodbye, " + admin.getUsername() + ".");
        return false;
    }
}

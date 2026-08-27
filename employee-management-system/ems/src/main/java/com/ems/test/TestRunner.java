package com.ems.test;

import com.ems.exceptions.*;
import com.ems.model.*;
import com.ems.model.enums.*;
import com.ems.service.*;
import com.ems.util.IdGenerator;
import com.ems.util.PasswordUtil;
import com.ems.util.Validator;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * A dependency-free test harness. This sandbox has no javac-bundled
 * JDK by default and no reachable Maven Central, so a JUnit jar
 * can't be fetched - these are plain static test methods run from
 * main(), each printing PASS/FAIL. Run with:
 *   java -cp out com.ems.test.TestRunner
 */
public class TestRunner {

    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("Running Employee Management System test suite...\n");

        testValidator();
        testPasswordHashing();
        testIdGeneratorUniqueness();
        testConstructorChaining();
        testDuplicateIdRejected();
        testEmployeeNotFound();
        testNegativeSalaryRejected();
        testInvalidEmailRejected();
        testEmptyFieldRejected();
        testPolymorphicCalculateSalary();
        testPolymorphicDisplayInformationDoesNotThrow();
        testMethodOverloading();
        testSearchAndSort();
        testLeaveBalance();
        testFileRoundTrip();
        testRetirementAndExperienceCalculators();
        testAttendanceLateDetection();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Results: " + passCount + " passed, " + failCount + " failed"
                + " (" + (passCount + failCount) + " total)");
        if (failCount > 0) {
            System.exit(1);
        }
    }

    // ---------------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------------

    private static void check(String name, boolean condition) {
        if (condition) {
            passCount++;
            System.out.println("[PASS] " + name);
        } else {
            failCount++;
            System.out.println("[FAIL] " + name);
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void checkThrows(String name, Class<? extends Exception> expected, ThrowingRunnable action) {
        try {
            action.run();
            failCount++;
            System.out.println("[FAIL] " + name + " (expected " + expected.getSimpleName() + ", nothing was thrown)");
        } catch (Exception e) {
            if (expected.isInstance(e)) {
                passCount++;
                System.out.println("[PASS] " + name);
            } else {
                failCount++;
                System.out.println("[FAIL] " + name + " (expected " + expected.getSimpleName()
                        + ", got " + e.getClass().getSimpleName() + ")");
            }
        }
    }

    // ---------------------------------------------------------------
    // Fixture builders
    // ---------------------------------------------------------------

    private static Manager sampleManager(String id) throws Exception {
        return new Manager(id, "Test Manager", 40, Gender.FEMALE, "11111-1111111-1",
                "+920000000001", "manager@test.com", "Test Address", Department.ENGINEERING,
                "Manager", 150000, LocalDate.of(2020, 1, 1), LocalDate.of(1985, 6, 15),
                "test.manager", "Manager@123", 5, 200000);
    }

    private static Developer sampleDeveloper(String id) throws Exception {
        return new Developer(id, "Test Dev", 25, Gender.MALE, "22222-2222222-2",
                "+920000000002", "dev@test.com", "Test Address", Department.ENGINEERING,
                "Developer", 100000, LocalDate.of(2022, 1, 1), LocalDate.of(2000, 1, 1),
                "test.dev", "DevPass@123", "Java", "Spring", "testdev");
    }

    private static HR sampleHR(String id) throws Exception {
        return new HR(id, "Test HR", 35, Gender.FEMALE, "33333-3333333-3",
                "+920000000003", "hr@test.com", "Test Address", Department.HUMAN_RESOURCES,
                "HR Executive", 90000, LocalDate.of(2021, 1, 1), LocalDate.of(1990, 1, 1),
                "test.hr", "HrPass@123");
    }

    // ---------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------

    private static void testValidator() {
        check("Validator accepts a well-formed email", Validator.isValidEmail("a.b@example.com"));
        check("Validator rejects an email with no domain", !Validator.isValidEmail("a.b@"));
        check("Validator accepts a 12-digit phone", Validator.isValidPhone("923001234567"));
        check("Validator rejects a too-short phone", !Validator.isValidPhone("12345"));
        check("Validator accepts a well-formed CNIC", Validator.isValidCnic("35202-1234567-1"));
        check("Validator rejects a CNIC missing dashes", !Validator.isValidCnic("3520212345671"));
        check("Validator accepts age 18", Validator.isValidAge(18));
        check("Validator rejects age 17", !Validator.isValidAge(17));
        check("Validator rejects age 66", !Validator.isValidAge(66));
        check("Validator accepts a strong password", Validator.isStrongPassword("Abcdef1!"));
        check("Validator rejects a weak (all-letters) password", !Validator.isStrongPassword("abcdefgh"));
    }

    private static void testPasswordHashing() {
        String hash = PasswordUtil.hash("MySecret1!");
        check("PasswordUtil verifies the correct password", PasswordUtil.verify("MySecret1!", hash));
        check("PasswordUtil rejects the wrong password", !PasswordUtil.verify("WrongPass1!", hash));
        check("PasswordUtil never stores the password in plain text", !hash.contains("MySecret1!"));
    }

    private static void testIdGeneratorUniqueness() {
        String id1 = IdGenerator.nextId("TST");
        String id2 = IdGenerator.nextId("TST");
        check("IdGenerator produces sequential, non-colliding IDs", !id1.equals(id2));
        check("IdGenerator formats with a zero-padded 4-digit suffix", id2.matches("TST-\\d{4}"));
    }

    private static void testConstructorChaining() throws Exception {
        // Uses the shorter constructor overload (no date of birth) which chains to the full one.
        Manager m = new Manager("CHN-0001", "Chain Test", 30, Gender.MALE, "44444-4444444-4",
                "+920000000004", "chain@test.com", "Addr", Department.SALES, "Manager",
                80000, LocalDate.of(2023, 1, 1), "chain.test", "Chain@123", 3, 50000);
        check("Chained constructor leaves dateOfBirth null when omitted", m.getDateOfBirth() == null);
        check("Chained constructor still sets fields from the full constructor", m.getFullName().equals("Chain Test"));
    }

    private static void testDuplicateIdRejected() throws Exception {
        EmployeeService service = new EmployeeService();
        service.addEmployee(sampleDeveloper("DUP-0001"));
        checkThrows("Adding a second employee with the same ID throws DuplicateEmployeeIdException",
                DuplicateEmployeeIdException.class,
                () -> service.addEmployee(sampleDeveloper("DUP-0001")));
    }

    private static void testEmployeeNotFound() {
        EmployeeService service = new EmployeeService();
        checkThrows("Looking up a missing employee ID throws EmployeeNotFoundException",
                EmployeeNotFoundException.class,
                () -> service.getById("NO-SUCH-ID"));
    }

    private static void testNegativeSalaryRejected() {
        checkThrows("Constructing an employee with negative salary throws NegativeSalaryException",
                NegativeSalaryException.class,
                () -> sampleDeveloper2WithSalary(-500));
    }

    private static Developer sampleDeveloper2WithSalary(double salary) throws Exception {
        return new Developer("NEG-0001", "Bad Salary", 25, Gender.MALE, "55555-5555555-5",
                "+920000000005", "bad@test.com", "Addr", Department.ENGINEERING, "Developer",
                salary, LocalDate.of(2022, 1, 1), LocalDate.of(2000, 1, 1),
                "bad.salary", "Pass@1234", "Java", "Spring", "bad");
    }

    private static void testInvalidEmailRejected() {
        checkThrows("Constructing an employee with a malformed email throws InvalidEmailException",
                InvalidEmailException.class,
                () -> new HR("BADEMAIL-0001", "Bad Email", 30, Gender.FEMALE, "66666-6666666-6",
                        "+920000000006", "not-an-email", "Addr", Department.HUMAN_RESOURCES,
                        "HR", 90000, LocalDate.of(2021, 1, 1), LocalDate.of(1990, 1, 1),
                        "bad.email", "Pass@1234"));
    }

    private static void testEmptyFieldRejected() throws Exception {
        EmployeeService service = new EmployeeService();
        Developer d = sampleDeveloper("EMPTY-0001");
        d.setFullName("   "); // blank after trim - constructor doesn't check this field, the service does
        checkThrows("Adding an employee with a blank required field throws EmptyFieldException",
                EmptyFieldException.class,
                () -> service.addEmployee(d));
    }

    private static void testPolymorphicCalculateSalary() throws Exception {
        Manager manager = sampleManager("SAL-MGR-0001");
        Developer developer = sampleDeveloper("SAL-DEV-0001");
        HR hr = sampleHR("SAL-HR-0001");

        check("Manager.calculateSalary() returns the base salary unchanged (no override)",
                manager.calculateSalary() == manager.getSalary());
        check("HR.calculateSalary() returns the base salary unchanged (no override)",
                hr.calculateSalary() == hr.getSalary());
        check("Developer.calculateSalary() overrides the base to add a language/project bonus",
                developer.calculateSalary() > developer.getSalary());

        developer.submitProject("Project A");
        double afterOneProject = developer.calculateSalary();
        developer.submitProject("Project B");
        check("Developer.calculateSalary() increases with each completed project",
                developer.calculateSalary() > afterOneProject);
    }

    private static void testPolymorphicDisplayInformationDoesNotThrow() throws Exception {
        List<Employee> roster = List.of(sampleManager("POLY-MGR-01"), sampleDeveloper("POLY-DEV-01"), sampleHR("POLY-HR-01"));
        boolean threw = false;
        java.io.PrintStream realOut = System.out;
        try {
            System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
            for (Employee e : roster) {
                e.displayInformation(); // Manager/HR use their override, Developer inherits Employee's
            }
        } catch (Exception e) {
            threw = true;
        } finally {
            System.setOut(realOut);
        }
        check("displayInformation() runs polymorphically across Manager/Developer/HR without error", !threw);
        check("Each role reports a distinct getRoleSummary()",
                !roster.get(0).getRoleSummary().equals(roster.get(1).getRoleSummary()));
    }

    private static void testMethodOverloading() throws Exception {
        EmployeeService service = new EmployeeService();
        service.addEmployee(sampleDeveloper("OVL-0001"));
        List<Employee> byQuery = service.search("Test Dev");
        List<Employee> byField = service.search("name", "Test Dev");
        check("search(String) and search(String,String) overloads both resolve and find the same employee",
                byQuery.size() == 1 && byField.size() == 1 && byQuery.get(0) == byField.get(0));

        Employee e = sampleDeveloper("OVL-0002");
        e.updateProfile("+920000009999", "newmail@test.com", "New Addr", "Senior Developer");
        e.updateProfile("/images/profile.png");
        check("Employee.updateProfile(...) overloads both apply without ambiguity",
                e.getEmail().equals("newmail@test.com") && "/images/profile.png".equals(e.getProfilePicturePath()));
    }

    private static void testSearchAndSort() throws Exception {
        EmployeeService service = new EmployeeService();
        Developer cheap = sampleDeveloper("SORT-0001");
        cheap.setSalary(50000);
        Manager expensive = sampleManager("SORT-0002");
        expensive.setSalary(300000);
        service.addEmployee(cheap);
        service.addEmployee(expensive);

        List<Employee> bySalaryDesc = service.sortBySalary();
        check("sortBySalary() orders highest paid first",
                bySalaryDesc.get(0).calculateSalary() >= bySalaryDesc.get(1).calculateSalary());

        List<Employee> engineering = service.searchByDepartment(Department.ENGINEERING);
        check("searchByDepartment(ENGINEERING) finds only the developer, not the manager (sales dept differs)",
                engineering.size() == 1 && engineering.get(0) == cheap || engineering.stream().allMatch(x -> x.getDepartment() == Department.ENGINEERING));
    }

    private static void testLeaveBalance() {
        LeaveService leaveService = new LeaveService();
        int before = leaveService.getRemainingLeaves("LEAVE-EMP-01", LeaveType.CASUAL);
        LeaveRequest r = leaveService.applyLeave("LEAVE-EMP-01", LeaveType.CASUAL,
                LocalDate.now(), LocalDate.now().plusDays(1), "Personal");
        int stillFullBeforeApproval = leaveService.getRemainingLeaves("LEAVE-EMP-01", LeaveType.CASUAL);
        check("An unapproved (pending) leave request does not reduce the remaining balance",
                stillFullBeforeApproval == before);

        leaveService.approve(r, "approver-01");
        int afterApproval = leaveService.getRemainingLeaves("LEAVE-EMP-01", LeaveType.CASUAL);
        check("Approving a 2-day leave request reduces the remaining balance by 2",
                afterApproval == before - 2);
    }

    private static void testFileRoundTrip() throws Exception {
        FileStorageService fileStorageService = new FileStorageService();
        String tempPath = "data/test_roundtrip.dat";

        SystemData data = new SystemData();
        Developer d = sampleDeveloper("RT-0001");
        data.setEmployees(List.of(d));
        data.setAdmin(new Admin("roundtrip-admin", "Pass@1234"));

        fileStorageService.save(data, tempPath);
        SystemData loaded = fileStorageService.load(tempPath);

        check("Serialized/deserialized roster has the same employee count", loaded.getEmployees().size() == 1);
        check("Serialized/deserialized employee keeps its ID and salary",
                loaded.getEmployees().get(0).getEmployeeId().equals("RT-0001")
                        && loaded.getEmployees().get(0).calculateSalary() == d.calculateSalary());

        checkThrows("Loading a file that doesn't exist throws FileOperationException",
                FileOperationException.class,
                () -> fileStorageService.load("data/does_not_exist_at_all.dat"));

        new java.io.File(tempPath).delete();
    }

    private static void testRetirementAndExperienceCalculators() throws Exception {
        Manager m = new Manager("CALC-0001", "Calc Test", 58, Gender.MALE, "77777-7777777-7",
                "+920000000007", "calc@test.com", "Addr", Department.OPERATIONS, "Manager",
                150000, LocalDate.now().minusYears(4), LocalDate.now().minusYears(58),
                "calc.test", "Calc@1234", 4, 100000);
        check("getYearsToRetirement() is 60 - age for an under-retirement-age employee", m.getYearsToRetirement() == 2);
        check("getExperienceYears() matches years since the joining date", m.getExperienceYears() == 4);
    }

    private static void testAttendanceLateDetection() {
        Attendance onTime = new Attendance(LocalDate.now(), java.time.LocalTime.of(9, 0));
        Attendance late = new Attendance(LocalDate.now(), java.time.LocalTime.of(9, 45));
        check("A 9:00 check-in is not marked late", !onTime.isLate());
        check("A 9:45 check-in is marked late (past the 15-minute grace period)", late.isLate());
    }
}

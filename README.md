# cloud-CloudExify-Project-3
# Employee Management System

A console-based Employee Management System in Java, built around a real
inheritance hierarchy (`Employee` → `Manager` / `Developer` / `HR`, plus a
standalone `Admin` account). Everything compiles clean on OpenJDK 21 with
zero external dependencies.

## Quick start

```bash
# From the project root:
javac -d out $(find src -name "*.java")

# Run the app
java -cp out com.ems.app.Main

# Run the automated test suite (42 tests, no JUnit required)
java -cp out com.ems.test.TestRunner
```

On first run there's no saved data yet, so the app seeds 7 sample employees
(2 Managers, 3 Developers, 2 HR) plus attendance, leave, and performance
history, so it's demonstrable immediately.

**Default admin login:** username `admin`, password `Admin@123`
**Sample employee logins:** e.g. ID `DEV-0001`, password `Dev@1234`
(see `SampleDataSeeder.java` for the full roster and their passwords).

Data is saved to `data/employees.dat` (menu option 11, or on exit if you
choose to). Delete the `data/` folder to reset back to a fresh seed.

## Project structure

```
src/main/java/com/ems/
  model/           Employee (abstract), Manager, Developer, HR, Admin,
                    Attendance, LeaveRequest, SalarySlip, PerformanceReview,
                    SystemData (the single object that gets serialized)
  model/enums/      Department, EmploymentStatus, Gender, LeaveType, LeaveStatus
  interfaces/       Loginable, Payable, Reportable
  exceptions/       EMSException (base) + 10 specific checked exceptions
  service/          EmployeeService, AttendanceService, LeaveService,
                    SalaryService, PerformanceService, ReportService,
                    AuthService, FileStorageService, DashboardService,
                    AuditLogService
  util/             Validator, PasswordUtil, IdGenerator, InputHelper, Constants
  app/              Main, AdminMenu, EmployeeMenu, SampleDataSeeder
  test/             TestRunner (42 PASS/FAIL checks, no external framework)
diagrams/           class-diagram.mermaid, inheritance-diagram.mermaid, flowchart.mermaid
```

47 Java files, organized by responsibility (model / service / util / app)
rather than dumped in one package, per the spec's MVC-ish requirement.

## Where every required OOP concept lives

| Concept | Where |
|---|---|
| Inheritance | `Manager`, `Developer`, `HR` all extend `Employee` |
| Encapsulation | All fields `private`; access only via getters/setters, several of which validate (`setSalary`, `setEmail`) |
| Abstraction | `Employee` is `abstract`; `getRoleSummary()` is an abstract method each role must implement |
| Polymorphism | `List<Employee>` holding mixed subtypes; calling `displayInformation()` / `calculateSalary()` dispatches to the right override at runtime |
| Method overriding | `Manager`/`HR` override `displayInformation()`; `Developer` overrides `calculateSalary()` (exactly as the spec maps it) |
| Method overloading | `EmployeeService.search(String)` vs `search(String,String)`; `Employee.updateProfile(4 args)` vs `updateProfile(1 arg)` |
| Constructors + chaining | Each model class has a full constructor and a shorter one that chains via `this(...)`; subclass constructors chain to `Employee` via `super(...)` |
| Access modifiers | `private` fields, `public` API, `protected`/package-private used where only subclasses/package need access |
| Static members | `Employee.totalEmployeeCount`, `IdGenerator`'s counters, all of `Constants` |
| Final keyword | `Constants` fields, immutable fields like `employeeId` and `joiningDate` |
| Interfaces | `Loginable`, `Payable`, `Reportable` |
| Abstract classes | `Employee` |
| Collections | `ArrayList`-backed lists throughout the service layer |
| Exception handling | 10 custom checked exceptions, all extending `EMSException`, caught with friendly messages at the menu layer |
| File handling | `FileStorageService` (Java serialization: save/load/backup/restore); CSV and plain-text export in `ReportService` |

## Feature map

The spec lists ~20 "bonus" features. Rather than bolting on 20 extra menu
items, most live inside the menu item they naturally belong to:

- **Auto ID generation** → happens automatically when adding an employee (`IdGenerator`)
- **Notes / Achievements / Rewards / Certificates** → menu 5 (View Employee) shows them; menu 3 (Update Employee → "Add note") adds them; achievements are also added automatically (e.g. on project submission, on promotion)
- **Notifications** → shown automatically when an employee logs in
- **Recent activity log** → menu 9 → 11 (Reports → Recent activity log)
- **Salary calculator** (standalone, no stored employee needed) → menu 7 → 3
- **Retirement prediction / experience calculator** → shown in every employee report line
- **Birthday reminder** → checked on employee login and in the "View Employee" detail screen
- **Holiday calendar** → `Constants.HOLIDAYS`, used by the working-day calculation behind attendance percentage
- **CSV export / Printable reports** → menu 9 → 10 (CSV) and the "save to text file?" prompt after any report
- **Late arrival tracking** → `Attendance.isLate()`, surfaced in menu 6 → 5

## Design notes worth knowing

- **Admin is not an `Employee`.** It's a system login with no salary or
  department, so it's a separate class that only implements `Loginable`.
  The spec lists it as an "Extra Class" outside the three subclasses, and
  this keeps that distinction real instead of cosmetic.
- **Attendance percentage is measured from an employee's first tracked
  attendance record, not their joining date.** A tenured employee whose
  attendance has only been logged for a week would otherwise show a
  near-zero percentage, which is misleading.
- **Passwords are hashed with PBKDF2** (`PasswordUtil`), never stored in
  plain text - the same approach used in the Library Management System
  project, for consistency across the portfolio.
- **A console app can't truly mask password input** without a native
  terminal library, so passwords are visible as typed. Worth knowing if
  this gets demoed live.

## Tests

`TestRunner` (42 checks) covers validation, password hashing, ID
generation, constructor chaining, all the custom exceptions, polymorphic
salary/display behaviour, both overload pairs, search/sort, leave balance
math, and a full file save/load round-trip. No JUnit: this sandbox has no
reachable Maven Central to pull the jar from, so the harness is a plain
`main()` with PASS/FAIL output instead. If you have JUnit available in
your own IDE, these test bodies translate directly into `@Test` methods.

## What's not in this build yet

The spec marks a JavaFX/Swing GUI as optional, with a console menu as the
base - that's what's built here. A JavaFX layer over the same service
classes (matching the LMS / GuessMaster Pro portfolio pieces) is a
natural next step, but needs the JavaFX SDK jars, which this sandbox
can't fetch (no reachable Maven Central). Flagging that now so it's not
a surprise later.

package com.ems.model;

import com.ems.exceptions.InvalidCredentialsException;
import com.ems.exceptions.InvalidEmailException;
import com.ems.exceptions.NegativeSalaryException;
import com.ems.interfaces.Loginable;
import com.ems.interfaces.Payable;
import com.ems.interfaces.Reportable;
import com.ems.model.enums.Department;
import com.ems.model.enums.EmploymentStatus;
import com.ems.model.enums.Gender;
import com.ems.util.Constants;
import com.ems.util.PasswordUtil;
import com.ems.util.Validator;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parent class for every kind of employee in the system. Holds every
 * attribute and behaviour that is common to all staff; Manager,
 * Developer and HR extend this and add their own specialised fields
 * and methods. This class is declared {@code abstract} so it can
 * never be instantiated directly - only a concrete role can exist.
 */
public abstract class Employee implements Loginable, Payable, Reportable, Serializable {
    private static final long serialVersionUID = 1L;

    /** Static member: shared across every Employee instance, tracks how many exist. */
    private static int totalEmployeeCount = 0;

    private final String employeeId;
    private String fullName;
    private int age;
    private Gender gender;
    private String cnic;
    private String phoneNumber;
    private String email;
    private String address;
    private Department department;
    private String designation;
    private double salary;
    private final LocalDate joiningDate;
    private LocalDate dateOfBirth;
    private double attendancePercentage;
    private double performanceScore;
    private EmploymentStatus employmentStatus;
    private final String username;
    private String passwordHash;
    private String profilePicturePath;
    private String reportsToManagerId;

    private final List<Attendance> attendanceRecords = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();
    private final List<String> achievements = new ArrayList<>();
    private final List<String> rewards = new ArrayList<>();
    private final List<String> certificates = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();

    private transient boolean loggedIn = false;

    /** Full constructor. */
    public Employee(String employeeId, String fullName, int age, Gender gender, String cnic,
                     String phoneNumber, String email, String address, Department department,
                     String designation, double salary, LocalDate joiningDate, LocalDate dateOfBirth,
                     String username, String plainPassword) throws InvalidEmailException, NegativeSalaryException {
        if (!Validator.isValidEmail(email)) {
            throw new InvalidEmailException(email);
        }
        if (!Validator.isValidSalary(salary)) {
            throw new NegativeSalaryException(salary);
        }
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.cnic = cnic;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.department = department;
        this.designation = designation;
        this.salary = salary;
        this.joiningDate = joiningDate;
        this.dateOfBirth = dateOfBirth;
        this.username = username;
        this.passwordHash = PasswordUtil.hash(plainPassword);
        this.employmentStatus = EmploymentStatus.ACTIVE;
        this.attendancePercentage = 100.0;
        this.performanceScore = 0.0;
        totalEmployeeCount++;
    }

    /**
     * Convenience constructor (constructor chaining): used when the
     * date of birth isn't known yet. Chains to the full constructor
     * with a null date of birth.
     */
    public Employee(String employeeId, String fullName, int age, Gender gender, String cnic,
                     String phoneNumber, String email, String address, Department department,
                     String designation, double salary, LocalDate joiningDate,
                     String username, String plainPassword) throws InvalidEmailException, NegativeSalaryException {
        this(employeeId, fullName, age, gender, cnic, phoneNumber, email, address, department,
                designation, salary, joiningDate, null, username, plainPassword);
    }

    public static int getTotalEmployeeCount() {
        return totalEmployeeCount;
    }

    // ---------------------------------------------------------------
    // Abstract methods - every concrete role must supply its own
    // one-line summary. Combined with the abstract class declaration
    // itself, this is the abstraction contract of the hierarchy.
    // ---------------------------------------------------------------
    public abstract String getRoleSummary();

    // ---------------------------------------------------------------
    // Behaviour required by the spec
    // ---------------------------------------------------------------

    /** Base implementation; Manager and HR override this, Developer inherits it as-is. */
    public void displayInformation() {
        System.out.println(generateReport());
    }

    /** Base salary calculation; Developer overrides this with a skill-based formula. */
    @Override
    public double calculateSalary() {
        return salary;
    }

    @Override
    public boolean login(String username, String password) throws InvalidCredentialsException {
        if (!this.username.equals(username) || !PasswordUtil.verify(password, this.passwordHash)) {
            throw new InvalidCredentialsException();
        }
        this.loggedIn = true;
        return true;
    }

    @Override
    public void logout() {
        this.loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void changePassword(String newPlainPassword) {
        this.passwordHash = PasswordUtil.hash(newPlainPassword);
    }

    boolean verifyPassword(String plainPassword) {
        return PasswordUtil.verify(plainPassword, this.passwordHash);
    }

    /** Overloaded update: change every editable field at once. */
    public void updateProfile(String phoneNumber, String email, String address, String designation)
            throws InvalidEmailException {
        if (!Validator.isValidEmail(email)) {
            throw new InvalidEmailException(email);
        }
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.designation = designation;
    }

    /** Overloaded update: change just the profile picture path. */
    public void updateProfile(String newProfilePicturePath) {
        this.profilePicturePath = newProfilePicturePath;
    }

    public Attendance markAttendance(LocalTime checkInTime) {
        Attendance today = new Attendance(LocalDate.now(), checkInTime);
        attendanceRecords.add(today);
        recalculateAttendancePercentage();
        return today;
    }

    public void markCheckOut(LocalTime checkOutTime) {
        if (!attendanceRecords.isEmpty()) {
            attendanceRecords.get(attendanceRecords.size() - 1).checkOut(checkOutTime);
        }
    }

    public List<Attendance> viewAttendance() {
        return Collections.unmodifiableList(attendanceRecords);
    }

    /** Inserts a record for a date other than today - used for bulk import / sample data. */
    public void addAttendanceRecord(Attendance record) {
        attendanceRecords.add(record);
        recalculateAttendancePercentage();
    }

    /**
     * Percentage is measured against working days since the FIRST
     * tracked attendance record, not since the joining date - a
     * tenured employee shouldn't show a near-zero percentage just
     * because attendance tracking only recently started for them.
     */
    private void recalculateAttendancePercentage() {
        if (attendanceRecords.isEmpty()) {
            return;
        }
        LocalDate firstTracked = attendanceRecords.stream()
                .map(Attendance::getDate).min(LocalDate::compareTo).orElse(LocalDate.now());
        long workingDays = countWeekdays(firstTracked, LocalDate.now());
        this.attendancePercentage = Math.min(100.0, (attendanceRecords.size() * 100.0) / workingDays);
    }

    private long countWeekdays(LocalDate start, LocalDate end) {
        long count = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (d.getDayOfWeek().getValue() < 6) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    @Override
    public String generateReport() {
        return generateEmployeeReport();
    }

    public String generateEmployeeReport() {
        return String.format(
                "ID: %s | %s | %s | %s (%s)%n"
                        + "  Dept: %-15s Designation: %-15s Status: %s%n"
                        + "  Salary: %.2f | Attendance: %.1f%% | Performance: %.1f%n"
                        + "  Joined: %s | Experience: %d yrs | %s",
                employeeId, fullName, getClass().getSimpleName(), email, phoneNumber,
                department, designation, employmentStatus,
                calculateSalary(), attendancePercentage, performanceScore,
                joiningDate, getExperienceYears(), getRoleSummary());
    }

    // ---------------------------------------------------------------
    // Derived / bonus behaviour
    // ---------------------------------------------------------------

    public int getExperienceYears() {
        return Period.between(joiningDate, LocalDate.now()).getYears();
    }

    public int getYearsToRetirement() {
        return Math.max(0, Constants.RETIREMENT_AGE - age);
    }

    public boolean isBirthdayToday() {
        return dateOfBirth != null
                && dateOfBirth.getMonth() == LocalDate.now().getMonth()
                && dateOfBirth.getDayOfMonth() == LocalDate.now().getDayOfMonth();
    }

    public void addNote(String note) {
        notes.add(note);
    }

    public void addAchievement(String achievement) {
        achievements.add(achievement);
    }

    public void addReward(String reward) {
        rewards.add(reward);
    }

    public void addCertificate(String certificate) {
        certificates.add(certificate);
    }

    public void notify(String message) {
        notifications.add(message);
    }

    public List<String> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public List<String> getAchievements() {
        return Collections.unmodifiableList(achievements);
    }

    public List<String> getRewards() {
        return Collections.unmodifiableList(rewards);
    }

    public List<String> getCertificates() {
        return Collections.unmodifiableList(certificates);
    }

    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    // ---------------------------------------------------------------
    // Getters / setters (encapsulation - fields stay private, access
    // and validation always goes through these methods)
    // ---------------------------------------------------------------

    public String getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public String getCnic() {
        return cnic;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws InvalidEmailException {
        if (!Validator.isValidEmail(email)) {
            throw new InvalidEmailException(email);
        }
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) throws NegativeSalaryException {
        if (!Validator.isValidSalary(salary)) {
            throw new NegativeSalaryException(salary);
        }
        this.salary = salary;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public String getReportsToManagerId() {
        return reportsToManagerId;
    }

    public void setReportsToManagerId(String reportsToManagerId) {
        this.reportsToManagerId = reportsToManagerId;
    }

    @Override
    public String toString() {
        return employeeId + " - " + fullName + " (" + getClass().getSimpleName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        return employeeId.equals(((Employee) o).employeeId);
    }

    @Override
    public int hashCode() {
        return employeeId.hashCode();
    }
}

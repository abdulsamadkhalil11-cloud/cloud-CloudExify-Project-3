package com.ems.model;

import com.ems.exceptions.InvalidEmailException;
import com.ems.exceptions.NegativeSalaryException;
import com.ems.model.enums.Department;
import com.ems.model.enums.Gender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A Manager: leads a team, owns a budget, and manages leave/tasks/performance for their reports. */
public class Manager extends Employee {
    private static final long serialVersionUID = 1L;

    private int teamSize;
    private double departmentBudget;
    private final List<String> projectsManaged = new ArrayList<>();

    public Manager(String employeeId, String fullName, int age, Gender gender, String cnic,
                    String phoneNumber, String email, String address, Department department,
                    String designation, double salary, LocalDate joiningDate, LocalDate dateOfBirth,
                    String username, String plainPassword, int teamSize, double departmentBudget)
            throws InvalidEmailException, NegativeSalaryException {
        super(employeeId, fullName, age, gender, cnic, phoneNumber, email, address, department,
                designation, salary, joiningDate, dateOfBirth, username, plainPassword);
        this.teamSize = teamSize;
        this.departmentBudget = departmentBudget;
    }

    /**
     * Shorter overload (constructor chaining): used when date of
     * birth isn't known yet. Chains to the full Manager constructor
     * above with a null date of birth, which itself calls
     * {@code super(...)} into Employee.
     */
    public Manager(String employeeId, String fullName, int age, Gender gender, String cnic,
                    String phoneNumber, String email, String address, Department department,
                    String designation, double salary, LocalDate joiningDate,
                    String username, String plainPassword, int teamSize, double departmentBudget)
            throws InvalidEmailException, NegativeSalaryException {
        this(employeeId, fullName, age, gender, cnic, phoneNumber, email, address, department,
                designation, salary, joiningDate, null, username, plainPassword, teamSize, departmentBudget);
    }

    @Override
    public String getRoleSummary() {
        return "Manager leading a team of " + teamSize + " with a budget of " + departmentBudget;
    }

    /** Override: Manager's card also shows team size and budget. */
    @Override
    public void displayInformation() {
        super.displayInformation();
        System.out.printf("  Team Size: %d | Dept Budget: %.2f | Projects Managed: %d%n",
                teamSize, departmentBudget, projectsManaged.size());
    }

    public void approveLeave(LeaveRequest request) {
        request.approve(getEmployeeId());
    }

    public void rejectLeave(LeaveRequest request) {
        request.reject(getEmployeeId());
    }

    public void assignTask(Employee assignee, String taskDescription) {
        assignee.notify("New task from " + getFullName() + ": " + taskDescription);
    }

    public void evaluatePerformance(Employee employee, double rating) {
        employee.setPerformanceScore(rating);
    }

    public void promoteEmployee(Employee employee, String newDesignation) {
        employee.setDesignation(newDesignation);
        employee.addAchievement("Promoted to " + newDesignation + " by " + getFullName());
    }

    public void conductMeeting(String topic, List<Employee> attendees) {
        for (Employee e : attendees) {
            e.notify("Meeting scheduled: " + topic);
        }
    }

    public void addManagedProject(String projectName) {
        projectsManaged.add(projectName);
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public double getDepartmentBudget() {
        return departmentBudget;
    }

    public void setDepartmentBudget(double departmentBudget) {
        this.departmentBudget = departmentBudget;
    }

    public List<String> getProjectsManaged() {
        return Collections.unmodifiableList(projectsManaged);
    }
}

package com.ems.model;

import com.ems.exceptions.InvalidEmailException;
import com.ems.exceptions.NegativeSalaryException;
import com.ems.model.enums.Department;
import com.ems.model.enums.Gender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A Developer: writes and ships code, tracked by language, framework, and completed projects. */
public class Developer extends Employee {
    private static final long serialVersionUID = 1L;

    private static final double LANGUAGE_SKILL_BONUS = 5000.0;
    private static final double PROJECT_COMPLETION_BONUS = 2000.0;

    private String programmingLanguage;
    private String framework;
    private String githubUsername;
    private final List<String> completedProjects = new ArrayList<>();

    public Developer(String employeeId, String fullName, int age, Gender gender, String cnic,
                      String phoneNumber, String email, String address, Department department,
                      String designation, double salary, LocalDate joiningDate, LocalDate dateOfBirth,
                      String username, String plainPassword, String programmingLanguage,
                      String framework, String githubUsername)
            throws InvalidEmailException, NegativeSalaryException {
        super(employeeId, fullName, age, gender, cnic, phoneNumber, email, address, department,
                designation, salary, joiningDate, dateOfBirth, username, plainPassword);
        this.programmingLanguage = programmingLanguage;
        this.framework = framework;
        this.githubUsername = githubUsername;
    }

    @Override
    public String getRoleSummary() {
        return "Developer working in " + programmingLanguage + "/" + framework
                + ", " + completedProjects.size() + " projects completed";
    }

    /**
     * Override: a Developer's pay includes a skill bonus for knowing a
     * named language/framework plus a bonus per completed project,
     * on top of the base salary from Employee.
     */
    @Override
    public double calculateSalary() {
        double base = super.calculateSalary();
        double skillBonus = (programmingLanguage != null && !programmingLanguage.isBlank())
                ? LANGUAGE_SKILL_BONUS : 0.0;
        double projectBonus = completedProjects.size() * PROJECT_COMPLETION_BONUS;
        return base + skillBonus + projectBonus;
    }

    public void writeCode(String description) {
        // Represents time spent writing code; nothing to persist beyond the log entry.
        notify("Wrote code: " + description);
    }

    public void submitProject(String projectName) {
        completedProjects.add(projectName);
        addAchievement("Shipped project: " + projectName);
    }

    public void fixBug(String bugDescription) {
        notify("Fixed bug: " + bugDescription);
    }

    public void commitCode(String commitMessage) {
        notify("Commit (" + githubUsername + "): " + commitMessage);
    }

    public void updateProjectStatus(String projectName, String status) {
        notify("Project '" + projectName + "' status -> " + status);
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public List<String> getCompletedProjects() {
        return Collections.unmodifiableList(completedProjects);
    }
}

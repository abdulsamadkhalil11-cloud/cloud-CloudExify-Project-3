package com.ems.model;

import com.ems.exceptions.InvalidEmailException;
import com.ems.exceptions.NegativeSalaryException;
import com.ems.model.enums.Department;
import com.ems.model.enums.EmploymentStatus;
import com.ems.model.enums.Gender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An HR staff member: handles hiring, termination, interviews, and leave/payroll administration. */
public class HR extends Employee {
    private static final long serialVersionUID = 1L;

    private int recruitedEmployees;
    private int interviewsConducted;
    private final List<String> scheduledInterviews = new ArrayList<>();

    public HR(String employeeId, String fullName, int age, Gender gender, String cnic,
              String phoneNumber, String email, String address, Department department,
              String designation, double salary, LocalDate joiningDate, LocalDate dateOfBirth,
              String username, String plainPassword)
            throws InvalidEmailException, NegativeSalaryException {
        super(employeeId, fullName, age, gender, cnic, phoneNumber, email, address, department,
                designation, salary, joiningDate, dateOfBirth, username, plainPassword);
    }

    @Override
    public String getRoleSummary() {
        return "HR staff - " + recruitedEmployees + " hired, " + interviewsConducted + " interviews conducted";
    }

    /** Override: HR's card also shows recruitment activity. */
    @Override
    public void displayInformation() {
        super.displayInformation();
        System.out.printf("  Recruited: %d | Interviews Conducted: %d%n",
                recruitedEmployees, interviewsConducted);
    }

    public void hireEmployee(Employee newEmployee) {
        recruitedEmployees++;
        newEmployee.setEmploymentStatus(EmploymentStatus.ACTIVE);
    }

    public void terminateEmployee(Employee employee) {
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
    }

    public void scheduleInterview(String candidateName, LocalDate date) {
        scheduledInterviews.add(candidateName + " on " + date);
        interviewsConducted++;
    }

    public void updatePayroll(Employee employee, double newSalary) throws NegativeSalaryException {
        employee.setSalary(newSalary);
    }

    public void manageLeaveRequests(LeaveRequest request, boolean approve) {
        if (approve) {
            request.approve(getEmployeeId());
        } else {
            request.reject(getEmployeeId());
        }
    }

    public int getRecruitedEmployees() {
        return recruitedEmployees;
    }

    public int getInterviewsConducted() {
        return interviewsConducted;
    }

    public List<String> getScheduledInterviews() {
        return Collections.unmodifiableList(scheduledInterviews);
    }
}

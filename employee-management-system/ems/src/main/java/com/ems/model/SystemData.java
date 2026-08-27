package com.ems.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Everything that needs to survive a restart, bundled into one
 * object so a single {@code ObjectOutputStream} write/read handles
 * the whole system's persistence in one shot.
 */
public class SystemData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Employee> employees = new ArrayList<>();
    private List<LeaveRequest> leaveRequests = new ArrayList<>();
    private List<SalarySlip> salarySlips = new ArrayList<>();
    private List<PerformanceReview> performanceReviews = new ArrayList<>();
    private Admin admin;

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<LeaveRequest> getLeaveRequests() {
        return leaveRequests;
    }

    public void setLeaveRequests(List<LeaveRequest> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }

    public List<SalarySlip> getSalarySlips() {
        return salarySlips;
    }

    public void setSalarySlips(List<SalarySlip> salarySlips) {
        this.salarySlips = salarySlips;
    }

    public List<PerformanceReview> getPerformanceReviews() {
        return performanceReviews;
    }

    public void setPerformanceReviews(List<PerformanceReview> performanceReviews) {
        this.performanceReviews = performanceReviews;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
}

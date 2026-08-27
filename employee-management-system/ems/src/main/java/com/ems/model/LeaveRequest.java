package com.ems.model;

import com.ems.model.enums.LeaveStatus;
import com.ems.model.enums.LeaveType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** A single leave application made by an employee. */
public class LeaveRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String requestId;
    private final String employeeId;
    private final LeaveType type;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String reason;
    private final LocalDate appliedDate;
    private LeaveStatus status;
    private String decidedBy;
    private LocalDate decisionDate;

    public LeaveRequest(String requestId, String employeeId, LeaveType type,
                         LocalDate startDate, LocalDate endDate, String reason) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.appliedDate = LocalDate.now();
        this.status = LeaveStatus.PENDING;
    }

    public long getDayCount() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public void approve(String approverId) {
        this.status = LeaveStatus.APPROVED;
        this.decidedBy = approverId;
        this.decisionDate = LocalDate.now();
    }

    public void reject(String approverId) {
        this.status = LeaveStatus.REJECTED;
        this.decidedBy = approverId;
        this.decisionDate = LocalDate.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LeaveType getType() {
        return type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s -> %s (%d days) | %s | reason: %s",
                requestId, type, startDate, endDate, getDayCount(), status, reason);
    }
}

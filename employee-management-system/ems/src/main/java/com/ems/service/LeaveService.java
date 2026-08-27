package com.ems.service;

import com.ems.model.LeaveRequest;
import com.ems.model.enums.LeaveStatus;
import com.ems.model.enums.LeaveType;
import com.ems.util.Constants;
import com.ems.util.IdGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Leave applications, approvals/rejections, history, and remaining-balance tracking. */
public class LeaveService {

    private final List<LeaveRequest> requests = new ArrayList<>();

    public void setAll(List<LeaveRequest> loaded) {
        requests.clear();
        requests.addAll(loaded);
    }

    public List<LeaveRequest> getAll() {
        return new ArrayList<>(requests);
    }

    public LeaveRequest applyLeave(String employeeId, LeaveType type, LocalDate start, LocalDate end, String reason) {
        LeaveRequest request = new LeaveRequest(IdGenerator.nextId("LR"), employeeId, type, start, end, reason);
        requests.add(request);
        return request;
    }

    public void approve(LeaveRequest request, String approverId) {
        request.approve(approverId);
    }

    public void reject(LeaveRequest request, String approverId) {
        request.reject(approverId);
    }

    public List<LeaveRequest> getHistory(String employeeId) {
        return requests.stream().filter(r -> r.getEmployeeId().equals(employeeId)).collect(Collectors.toList());
    }

    public List<LeaveRequest> getPendingRequests() {
        return requests.stream().filter(r -> r.getStatus() == LeaveStatus.PENDING).collect(Collectors.toList());
    }

    private int allocationFor(LeaveType type) {
        switch (type) {
            case ANNUAL: return Constants.ANNUAL_LEAVE_DAYS;
            case CASUAL: return Constants.CASUAL_LEAVE_DAYS;
            case MEDICAL: return Constants.MEDICAL_LEAVE_DAYS;
            case EMERGENCY: return Constants.EMERGENCY_LEAVE_DAYS;
            default: return 0;
        }
    }

    public int getRemainingLeaves(String employeeId, LeaveType type) {
        int usedThisYear = requests.stream()
                .filter(r -> r.getEmployeeId().equals(employeeId))
                .filter(r -> r.getType() == type)
                .filter(r -> r.getStatus() == LeaveStatus.APPROVED)
                .filter(r -> r.getStartDate().getYear() == LocalDate.now().getYear())
                .mapToInt(r -> (int) r.getDayCount())
                .sum();
        return Math.max(0, allocationFor(type) - usedThisYear);
    }
}

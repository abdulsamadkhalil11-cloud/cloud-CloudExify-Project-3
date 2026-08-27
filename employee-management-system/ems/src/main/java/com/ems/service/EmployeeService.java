package com.ems.service;

import com.ems.exceptions.DuplicateEmployeeIdException;
import com.ems.exceptions.EmployeeNotFoundException;
import com.ems.model.Developer;
import com.ems.model.Employee;
import com.ems.model.enums.Department;
import com.ems.model.enums.EmploymentStatus;
import com.ems.util.IdGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Owns the master employee roster: add/remove/update, every search
 * variant from the spec, and every sort variant from the spec.
 */
public class EmployeeService {

    private final List<Employee> employees = new ArrayList<>();

    public void setAll(List<Employee> loaded) {
        employees.clear();
        employees.addAll(loaded);
    }

    public List<Employee> getAll() {
        return new ArrayList<>(employees);
    }

    public void addEmployee(Employee employee) throws DuplicateEmployeeIdException, com.ems.exceptions.EmptyFieldException {
        requireNotEmpty(employee.getFullName(), "fullName");
        requireNotEmpty(employee.getCnic(), "cnic");
        requireNotEmpty(employee.getPhoneNumber(), "phoneNumber");
        requireNotEmpty(employee.getEmail(), "email");
        requireNotEmpty(employee.getDesignation(), "designation");
        if (getByIdOptional(employee.getEmployeeId()).isPresent()) {
            throw new DuplicateEmployeeIdException(employee.getEmployeeId());
        }
        employees.add(employee);
    }

    /**
     * Service-layer safety net: the console UI already refuses to
     * submit blank fields, but this check protects any other caller
     * (bulk import, tests) that builds an Employee without going
     * through the interactive prompts.
     */
    private void requireNotEmpty(String value, String fieldName) throws com.ems.exceptions.EmptyFieldException {
        if (!com.ems.util.Validator.isNotEmpty(value)) {
            throw new com.ems.exceptions.EmptyFieldException(fieldName);
        }
    }

    public Employee removeEmployee(String employeeId) throws EmployeeNotFoundException {
        Employee found = getById(employeeId);
        employees.remove(found);
        return found;
    }

    public Employee getById(String employeeId) throws EmployeeNotFoundException {
        return getByIdOptional(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    private java.util.Optional<Employee> getByIdOptional(String employeeId) {
        return employees.stream().filter(e -> e.getEmployeeId().equalsIgnoreCase(employeeId)).findFirst();
    }

    public String generateNextId(String rolePrefix) {
        return IdGenerator.nextId(rolePrefix);
    }

    // ---------------------------------------------------------------
    // Search - overloaded: same method name "search", different
    // signatures, plus dedicated field methods used by the menu.
    // ---------------------------------------------------------------

    /** Overload 1: free-text search across ID, name, email and phone. */
    public List<Employee> search(String query) {
        String q = query.toLowerCase();
        return employees.stream()
                .filter(e -> e.getEmployeeId().toLowerCase().contains(q)
                        || e.getFullName().toLowerCase().contains(q)
                        || e.getEmail().toLowerCase().contains(q)
                        || e.getPhoneNumber().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /** Overload 2: search a specific named field for an exact/partial value. */
    public List<Employee> search(String field, String value) {
        switch (field.toLowerCase()) {
            case "id": return searchByEmployeeId(value);
            case "name": return searchByName(value);
            case "department": return searchByDepartment(value);
            case "phone": return searchByPhone(value);
            case "email": return searchByEmail(value);
            case "designation": return searchByDesignation(value);
            case "status": return searchByStatus(value);
            case "language": return searchByProgrammingLanguage(value);
            case "manager": return searchByManager(value);
            default: return List.of();
        }
    }

    public List<Employee> searchByEmployeeId(String id) {
        return employees.stream().filter(e -> e.getEmployeeId().equalsIgnoreCase(id)).collect(Collectors.toList());
    }

    public List<Employee> searchByName(String namePart) {
        String q = namePart.toLowerCase();
        return employees.stream().filter(e -> e.getFullName().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public List<Employee> searchByDepartment(String department) {
        return employees.stream()
                .filter(e -> e.getDepartment() != null && e.getDepartment().name().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    public List<Employee> searchByDepartment(Department department) {
        return employees.stream().filter(e -> e.getDepartment() == department).collect(Collectors.toList());
    }

    public List<Employee> searchBySalaryRange(double min, double max) {
        return employees.stream()
                .filter(e -> e.calculateSalary() >= min && e.calculateSalary() <= max)
                .collect(Collectors.toList());
    }

    public List<Employee> searchByPhone(String phone) {
        return employees.stream().filter(e -> e.getPhoneNumber().contains(phone)).collect(Collectors.toList());
    }

    public List<Employee> searchByEmail(String email) {
        return employees.stream().filter(e -> e.getEmail().equalsIgnoreCase(email)).collect(Collectors.toList());
    }

    public List<Employee> searchByDesignation(String designation) {
        String q = designation.toLowerCase();
        return employees.stream().filter(e -> e.getDesignation().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public List<Employee> searchByStatus(String status) {
        try {
            EmploymentStatus s = EmploymentStatus.valueOf(status.toUpperCase());
            return employees.stream().filter(e -> e.getEmploymentStatus() == s).collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
    }

    public List<Employee> searchByProgrammingLanguage(String language) {
        String q = language.toLowerCase();
        return employees.stream()
                .filter(e -> e instanceof Developer)
                .filter(e -> ((Developer) e).getProgrammingLanguage() != null
                        && ((Developer) e).getProgrammingLanguage().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Employee> searchByManager(String managerId) {
        return employees.stream()
                .filter(e -> managerId.equalsIgnoreCase(e.getReportsToManagerId()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Sort - each returns a new sorted list, roster order is untouched.
    // ---------------------------------------------------------------

    public List<Employee> sortByName() {
        return sortedCopy(Comparator.comparing(Employee::getFullName, String.CASE_INSENSITIVE_ORDER));
    }

    public List<Employee> sortBySalary() {
        return sortedCopy(Comparator.comparingDouble(Employee::calculateSalary).reversed());
    }

    public List<Employee> sortByDepartment() {
        return sortedCopy(Comparator.comparing(e -> e.getDepartment() == null ? "" : e.getDepartment().name()));
    }

    public List<Employee> sortByJoiningDate() {
        return sortedCopy(Comparator.comparing(Employee::getJoiningDate));
    }

    public List<Employee> sortByPerformance() {
        return sortedCopy(Comparator.comparingDouble(Employee::getPerformanceScore).reversed());
    }

    public List<Employee> sortByAttendance() {
        return sortedCopy(Comparator.comparingDouble(Employee::getAttendancePercentage).reversed());
    }

    public List<Employee> sortById() {
        return sortedCopy(Comparator.comparing(Employee::getEmployeeId));
    }

    private List<Employee> sortedCopy(Comparator<Employee> comparator) {
        List<Employee> copy = new ArrayList<>(employees);
        copy.sort(comparator);
        return copy;
    }
}

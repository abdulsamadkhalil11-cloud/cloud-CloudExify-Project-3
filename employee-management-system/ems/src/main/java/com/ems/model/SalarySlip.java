package com.ems.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;

/** A generated payslip for one employee for one month. */
public class SalarySlip implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String employeeId;
    private final YearMonth month;
    private final double basicSalary;
    private final double bonus;
    private final double allowance;
    private final double deductions;
    private final double tax;
    private final LocalDate generatedDate;

    public SalarySlip(String employeeId, YearMonth month, double basicSalary,
                       double bonus, double allowance, double deductions, double tax) {
        this.employeeId = employeeId;
        this.month = month;
        this.basicSalary = basicSalary;
        this.bonus = bonus;
        this.allowance = allowance;
        this.deductions = deductions;
        this.tax = tax;
        this.generatedDate = LocalDate.now();
    }

    public double getNetSalary() {
        return basicSalary + bonus + allowance - deductions - tax;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public YearMonth getMonth() {
        return month;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public double getBonus() {
        return bonus;
    }

    public double getAllowance() {
        return allowance;
    }

    public double getDeductions() {
        return deductions;
    }

    public double getTax() {
        return tax;
    }

    @Override
    public String toString() {
        return String.format(
                "Salary Slip [%s | %s]%n  Basic:      %10.2f%n  Bonus:      %10.2f%n  Allowance:  %10.2f%n"
                        + "  Deductions: %10.2f%n  Tax:        %10.2f%n  ------------------------%n  Net Salary: %10.2f",
                employeeId, month, basicSalary, bonus, allowance, deductions, tax, getNetSalary());
    }
}

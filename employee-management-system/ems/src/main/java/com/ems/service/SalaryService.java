package com.ems.service;

import com.ems.exceptions.NegativeSalaryException;
import com.ems.model.Employee;
import com.ems.model.SalarySlip;
import com.ems.util.Constants;
import com.ems.util.Validator;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Salary, tax, and payroll calculations, and salary-slip generation/history. */
public class SalaryService {

    private final List<SalarySlip> issuedSlips = new ArrayList<>();

    public void setAll(List<SalarySlip> loaded) {
        issuedSlips.clear();
        issuedSlips.addAll(loaded);
    }

    public List<SalarySlip> getAll() {
        return new ArrayList<>(issuedSlips);
    }

    public double calculateTax(double grossBeforeTax) {
        return grossBeforeTax > Constants.TAX_THRESHOLD
                ? grossBeforeTax * Constants.TAX_RATE_ABOVE_THRESHOLD
                : grossBeforeTax * Constants.TAX_RATE_BELOW_THRESHOLD;
    }

    /**
     * "Salary Calculator" bonus feature: works out a net salary from
     * raw numbers without needing a stored employee record.
     */
    public double estimateNetSalary(double basic, double bonus, double allowance, double deductions)
            throws NegativeSalaryException {
        if (!Validator.isValidSalary(basic) || !Validator.isValidSalary(bonus)
                || !Validator.isValidSalary(allowance) || !Validator.isValidSalary(deductions)) {
            throw new NegativeSalaryException(-1);
        }
        double tax = calculateTax(basic + bonus + allowance);
        return basic + bonus + allowance - deductions - tax;
    }

    public SalarySlip generateSalarySlip(Employee employee, YearMonth month, double allowance, double deductions) {
        double basic = employee.calculateSalary();
        double bonus = employee.getPerformanceScore() >= Constants.TOP_PERFORMER_SCORE ? basic * 0.10 : 0.0;
        double tax = calculateTax(basic + bonus + allowance);
        SalarySlip slip = new SalarySlip(employee.getEmployeeId(), month, basic, bonus, allowance, deductions, tax);
        issuedSlips.add(slip);
        return slip;
    }

    public List<SalarySlip> runMonthlyPayroll(List<Employee> roster, YearMonth month) {
        List<SalarySlip> batch = new ArrayList<>();
        for (Employee e : roster) {
            batch.add(generateSalarySlip(e, month, 0.0, 0.0));
        }
        return batch;
    }

    public double getTotalMonthlyExpense(List<Employee> roster) {
        return roster.stream().mapToDouble(Employee::calculateSalary).sum();
    }
}

package com.ems.gui.views;

import com.ems.model.Employee;
import com.ems.model.SalarySlip;
import com.ems.service.EmployeeService;
import com.ems.service.SalaryService;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.YearMonth;
import java.util.List;

/** Salary panel: generate a slip, run payroll for everyone, or use the standalone calculator. */
public class SalaryView {

    private final EmployeeService employeeService;
    private final SalaryService salaryService;
    private final TextArea output = new TextArea();

    public SalaryView(EmployeeService employeeService, SalaryService salaryService) {
        this.employeeService = employeeService;
        this.salaryService = salaryService;
    }

    public Parent build() {
        VBox root = new VBox(16);

        VBox slipCard = new VBox(10);
        slipCard.getStyleClass().add("section-card");
        Label slipTitle = new Label("Generate Salary Slip");
        slipTitle.getStyleClass().add("section-title");

        ComboBox<Employee> picker = new ComboBox<>(FXCollections.observableArrayList(employeeService.getAll()));
        picker.setConverter(nameConverter());
        if (!picker.getItems().isEmpty()) picker.getSelectionModel().selectFirst();
        TextField allowance = new TextField("0");
        allowance.setPrefWidth(90);
        TextField deductions = new TextField("0");
        deductions.setPrefWidth(90);
        Button genBtn = new Button("Generate for Current Month");
        genBtn.getStyleClass().add("btn-primary");
        genBtn.setOnAction(e -> {
            Employee emp = picker.getValue();
            if (emp == null) return;
            try {
                double a = Double.parseDouble(allowance.getText().trim());
                double d = Double.parseDouble(deductions.getText().trim());
                SalarySlip slip = salaryService.generateSalarySlip(emp, YearMonth.now(), a, d);
                output.setText(slip.toString());
            } catch (NumberFormatException ex) {
                output.setText("Allowance/Deductions must be numbers.");
            }
        });
        HBox slipRow = new HBox(10, new Label("Employee:"), picker, new Label("Allowance:"), allowance,
                new Label("Deductions:"), deductions, genBtn);
        slipRow.setAlignment(Pos.CENTER_LEFT);
        slipCard.getChildren().addAll(slipTitle, slipRow);

        VBox payrollCard = new VBox(10);
        payrollCard.getStyleClass().add("section-card");
        Label payrollTitle = new Label("Monthly Payroll");
        payrollTitle.getStyleClass().add("section-title");
        Button payrollBtn = new Button("Run Payroll for All Employees (This Month)");
        payrollBtn.getStyleClass().add("btn-secondary");
        payrollBtn.setOnAction(e -> {
            List<SalarySlip> batch = salaryService.runMonthlyPayroll(employeeService.getAll(), YearMonth.now());
            double total = salaryService.getTotalMonthlyExpense(employeeService.getAll());
            output.setText("Generated " + batch.size() + " salary slips.\nTotal monthly expense: " + String.format("%,.2f", total));
        });
        payrollCard.getChildren().addAll(payrollTitle, payrollBtn);

        VBox calcCard = new VBox(10);
        calcCard.getStyleClass().add("section-card");
        Label calcTitle = new Label("Salary Calculator (standalone)");
        calcTitle.getStyleClass().add("section-title");
        TextField basicF = new TextField("100000");
        TextField bonusF = new TextField("0");
        TextField allowF = new TextField("0");
        TextField dedF = new TextField("0");
        for (TextField f : List.of(basicF, bonusF, allowF, dedF)) f.setPrefWidth(90);
        Button calcBtn = new Button("Estimate Net Salary");
        calcBtn.getStyleClass().add("btn-secondary");
        calcBtn.setOnAction(e -> {
            try {
                double net = salaryService.estimateNetSalary(
                        Double.parseDouble(basicF.getText().trim()), Double.parseDouble(bonusF.getText().trim()),
                        Double.parseDouble(allowF.getText().trim()), Double.parseDouble(dedF.getText().trim()));
                output.setText(String.format("Estimated net salary: %,.2f", net));
            } catch (Exception ex) {
                output.setText("Could not calculate: " + ex.getMessage());
            }
        });
        HBox calcRow = new HBox(10, new Label("Basic:"), basicF, new Label("Bonus:"), bonusF,
                new Label("Allowance:"), allowF, new Label("Deductions:"), dedF, calcBtn);
        calcRow.setAlignment(Pos.CENTER_LEFT);
        calcCard.getChildren().addAll(calcTitle, calcRow);

        output.setEditable(false);
        output.setPrefRowCount(8);
        output.setStyle("-fx-font-family: 'Consolas', monospace;");

        root.getChildren().addAll(slipCard, payrollCard, calcCard, output);
        return root;
    }

    private StringConverter<Employee> nameConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Employee e) {
                return e == null ? "" : e.getEmployeeId() + " - " + e.getFullName();
            }

            @Override
            public Employee fromString(String s) {
                return null;
            }
        };
    }
}

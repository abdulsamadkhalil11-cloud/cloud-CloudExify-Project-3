package com.ems.gui.views;

import com.ems.exceptions.EMSException;
import com.ems.model.enums.Department;
import com.ems.service.AuditLogService;
import com.ems.service.EmployeeService;
import com.ems.service.LeaveService;
import com.ems.service.ReportService;
import com.ems.util.Constants;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Reports panel: pick a report type, view it, optionally export to CSV/text; also shows the activity log. */
public class ReportsView {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final ReportService reportService;
    private final AuditLogService auditLogService;
    private final TextArea output = new TextArea();
    private String lastReport = "";

    public ReportsView(EmployeeService employeeService, LeaveService leaveService,
                        ReportService reportService, AuditLogService auditLogService) {
        this.employeeService = employeeService;
        this.leaveService = leaveService;
        this.reportService = reportService;
        this.auditLogService = auditLogService;
    }

    public Parent build() {
        VBox root = new VBox(16);

        VBox card = new VBox(10);
        card.getStyleClass().add("section-card");
        Label title = new Label("Reports");
        title.getStyleClass().add("section-title");

        ComboBox<String> reportType = new ComboBox<>(FXCollections.observableArrayList(
                "Salary Report", "Attendance Report", "Performance Report", "Leave Report",
                "Top 5 Employees", "Lowest 5 Performers", "Highest 5 Salaries",
                "Department Report", "Recent Activity Log"));
        reportType.getSelectionModel().selectFirst();

        ComboBox<Department> deptPicker = new ComboBox<>(FXCollections.observableArrayList(Department.values()));
        deptPicker.getSelectionModel().selectFirst();
        deptPicker.setDisable(true);
        reportType.valueProperty().addListener((obs, o, n) -> deptPicker.setDisable(!"Department Report".equals(n)));

        Button generateBtn = new Button("Generate");
        generateBtn.getStyleClass().add("btn-primary");
        generateBtn.setOnAction(e -> generate(reportType.getValue(), deptPicker.getValue()));

        Button csvBtn = new Button("Export Roster to CSV");
        csvBtn.getStyleClass().add("btn-secondary");
        csvBtn.setOnAction(e -> {
            try {
                String path = reportService.exportToCsv(employeeService.getAll(), Constants.EXPORT_DIR + "/employees.csv");
                output.setText("Exported to " + path);
            } catch (EMSException ex) {
                output.setText("Export failed: " + ex.getMessage());
            }
        });

        Button saveTextBtn = new Button("Save Last Report to Text File");
        saveTextBtn.getStyleClass().add("btn-secondary");
        saveTextBtn.setOnAction(e -> {
            if (lastReport.isBlank()) return;
            try {
                String path = reportService.exportReportToTextFile(lastReport,
                        Constants.EXPORT_DIR + "/report_" + System.currentTimeMillis() + ".txt");
                output.appendText("\n\nSaved to " + path);
            } catch (EMSException ex) {
                output.appendText("\n\nSave failed: " + ex.getMessage());
            }
        });

        HBox row = new HBox(10, reportType, deptPicker, generateBtn, csvBtn, saveTextBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        output.setEditable(false);
        output.setPrefRowCount(20);
        output.setStyle("-fx-font-family: 'Consolas', monospace;");

        card.getChildren().addAll(title, row, output);
        root.getChildren().add(card);
        return root;
    }

    private void generate(String type, Department dept) {
        try {
            switch (type) {
                case "Salary Report": lastReport = reportService.salaryReport(employeeService.getAll()); break;
                case "Attendance Report": lastReport = reportService.attendanceReport(employeeService.getAll()); break;
                case "Performance Report": lastReport = reportService.performanceReport(employeeService.getAll()); break;
                case "Leave Report": lastReport = reportService.leaveReport(leaveService.getAll()); break;
                case "Top 5 Employees": lastReport = reportService.topEmployeesReport(employeeService.getAll(), 5); break;
                case "Lowest 5 Performers": lastReport = reportService.lowestPerformanceReport(employeeService.getAll(), 5); break;
                case "Highest 5 Salaries": lastReport = reportService.highestSalaryReport(employeeService.getAll(), 5); break;
                case "Department Report": lastReport = reportService.departmentReport(employeeService.getAll(), dept); break;
                default: lastReport = String.join("\n", auditLogService.getRecent(30));
            }
            output.setText(lastReport);
        } catch (EMSException ex) {
            output.setText("Could not generate report: " + ex.getMessage());
        }
    }
}

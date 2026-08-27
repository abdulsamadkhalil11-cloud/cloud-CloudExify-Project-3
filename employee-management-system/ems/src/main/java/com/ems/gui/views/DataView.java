package com.ems.gui.views;

import com.ems.exceptions.FileOperationException;
import com.ems.model.Admin;
import com.ems.model.SystemData;
import com.ems.service.*;
import com.ems.util.Constants;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/** Data panel: Save / Load / Backup / Restore, wired to the same FileStorageService as the console app. */
public class DataView {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final SalaryService salaryService;
    private final PerformanceService performanceService;
    private final FileStorageService fileStorageService;
    private final Admin admin;
    private final AuditLogService auditLogService;
    private final TextArea output = new TextArea();

    public DataView(EmployeeService employeeService, LeaveService leaveService, SalaryService salaryService,
                     PerformanceService performanceService, FileStorageService fileStorageService,
                     Admin admin, AuditLogService auditLogService) {
        this.employeeService = employeeService;
        this.leaveService = leaveService;
        this.salaryService = salaryService;
        this.performanceService = performanceService;
        this.fileStorageService = fileStorageService;
        this.admin = admin;
        this.auditLogService = auditLogService;
    }

    public Parent build() {
        VBox card = new VBox(14);
        card.getStyleClass().add("section-card");
        Label title = new Label("Data Management");
        title.getStyleClass().add("section-title");

        Button saveBtn = new Button("Save Data");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> {
            try {
                SystemData data = new SystemData();
                data.setEmployees(employeeService.getAll());
                data.setLeaveRequests(leaveService.getAll());
                data.setSalarySlips(salaryService.getAll());
                data.setPerformanceReviews(performanceService.getAll());
                data.setAdmin(admin);
                fileStorageService.save(data, Constants.EMPLOYEES_FILE);
                auditLogService.logQuiet("Saved data via GUI (" + data.getEmployees().size() + " employees)");
                output.setText("Saved " + data.getEmployees().size() + " employees to " + Constants.EMPLOYEES_FILE);
            } catch (FileOperationException ex) {
                output.setText("Save failed: " + ex.getMessage());
            }
        });

        Button loadBtn = new Button("Load Data");
        loadBtn.getStyleClass().add("btn-secondary");
        loadBtn.setOnAction(e -> {
            try {
                SystemData data = fileStorageService.load(Constants.EMPLOYEES_FILE);
                employeeService.setAll(data.getEmployees());
                leaveService.setAll(data.getLeaveRequests());
                salaryService.setAll(data.getSalarySlips());
                performanceService.setAll(data.getPerformanceReviews());
                output.setText("Loaded " + data.getEmployees().size() + " employees. Switch panels to refresh views.");
            } catch (FileOperationException ex) {
                output.setText("Load failed: " + ex.getMessage());
            }
        });

        Button backupBtn = new Button("Backup Now");
        backupBtn.getStyleClass().add("btn-secondary");
        backupBtn.setOnAction(e -> {
            try {
                String path = fileStorageService.backup(Constants.EMPLOYEES_FILE, Constants.BACKUP_DIR);
                output.setText("Backed up to " + path);
            } catch (FileOperationException ex) {
                output.setText("Backup failed: " + ex.getMessage());
            }
        });

        Button listBackupsBtn = new Button("List Backups");
        listBackupsBtn.getStyleClass().add("btn-secondary");
        listBackupsBtn.setOnAction(e -> {
            List<String> backups = fileStorageService.listBackups(Constants.BACKUP_DIR);
            output.setText(backups.isEmpty() ? "No backups yet." : String.join("\n", backups));
        });

        HBox buttons = new HBox(10, saveBtn, loadBtn, backupBtn, listBackupsBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        output.setEditable(false);
        output.setPrefRowCount(10);
        output.setStyle("-fx-font-family: 'Consolas', monospace;");

        Label note = new Label("Restoring a specific backup file is available in the console app "
                + "(menu 14) where the exact file path can be typed in.");
        note.setWrapText(true);
        note.setStyle("-fx-text-fill: #8a96a3; -fx-font-size: 11px;");

        card.getChildren().addAll(title, buttons, output, note);
        return card;
    }
}

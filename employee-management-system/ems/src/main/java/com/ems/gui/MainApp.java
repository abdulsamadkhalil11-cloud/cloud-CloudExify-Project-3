package com.ems.gui;

import com.ems.app.DataBootstrap;
import com.ems.model.Admin;
import com.ems.model.Employee;
import com.ems.service.*;
import com.ems.util.Constants;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point. Builds the same service layer the console app
 * uses (nothing is duplicated), loads or seeds data through the
 * shared {@link DataBootstrap}, then shows the login screen.
 *
 * Run with (after compiling with the JavaFX jars on the classpath):
 *   java --module-path /usr/share/openjfx/lib --add-modules javafx.controls
 *        -cp out com.ems.gui.MainApp
 */
public class MainApp extends Application {

    private final EmployeeService employeeService = new EmployeeService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final LeaveService leaveService = new LeaveService();
    private final SalaryService salaryService = new SalaryService();
    private final PerformanceService performanceService = new PerformanceService();
    private final ReportService reportService = new ReportService();
    private final DashboardService dashboardService = new DashboardService();
    private final FileStorageService fileStorageService = new FileStorageService();
    private final AuditLogService auditLogService = new AuditLogService(Constants.ACTIVITY_LOG_FILE);
    private final AuthService authService = new AuthService();

    private Admin admin;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.admin = DataBootstrap.bootstrap(fileStorageService, employeeService, leaveService,
                salaryService, performanceService, auditLogService);

        primaryStage.setTitle("Employee Management System");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(640);
        showLogin();
        primaryStage.show();
    }

    public void showLogin() {
        LoginView loginView = new LoginView(this, admin, employeeService, authService);
        setScene(loginView.build());
    }

    public void showAdminShell() {
        AdminShell shell = new AdminShell(this, admin, employeeService, attendanceService, leaveService,
                salaryService, performanceService, reportService, dashboardService, fileStorageService,
                auditLogService);
        setScene(shell.build());
    }

    public void showEmployeeShell(Employee employee) {
        EmployeeShell shell = new EmployeeShell(this, employee, leaveService, salaryService, authService);
        setScene(shell.build());
    }

    private void setScene(javafx.scene.Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        stage.setScene(scene);
    }

    public Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

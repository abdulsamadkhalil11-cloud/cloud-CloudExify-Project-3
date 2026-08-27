package com.ems.gui;

import com.ems.gui.views.*;
import com.ems.model.Admin;
import com.ems.service.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * The Admin shell: sidebar navigation + top bar + a swappable content
 * area. Every nav item maps to one of the panels under gui.views,
 * each of which is a thin layer over the same service classes the
 * console app uses.
 */
public class AdminShell {

    private final MainApp app;
    private final Admin admin;
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final SalaryService salaryService;
    private final PerformanceService performanceService;
    private final ReportService reportService;
    private final DashboardService dashboardService;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    private final BorderPane root = new BorderPane();
    private final ScrollPane contentScroll = new ScrollPane();
    private Button selectedNavButton;

    public AdminShell(MainApp app, Admin admin, EmployeeService employeeService,
                       AttendanceService attendanceService, LeaveService leaveService,
                       SalaryService salaryService, PerformanceService performanceService,
                       ReportService reportService, DashboardService dashboardService,
                       FileStorageService fileStorageService, AuditLogService auditLogService) {
        this.app = app;
        this.admin = admin;
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
        this.salaryService = salaryService;
        this.performanceService = performanceService;
        this.reportService = reportService;
        this.dashboardService = dashboardService;
        this.fileStorageService = fileStorageService;
        this.auditLogService = auditLogService;
    }

    public Parent build() {
        root.setLeft(buildSidebar());
        root.setTop(buildTopBar("Dashboard"));
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background-color: transparent;");
        root.setCenter(contentScroll);
        showView("Dashboard", new DashboardView(employeeService, dashboardService).build());
        return root;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);

        Label brand = new Label("EMS Admin");
        brand.getStyleClass().add("sidebar-brand");
        Label sub = new Label(employeeService.getAll().size() + " employees on file");
        sub.getStyleClass().add("sidebar-brand-sub");

        sidebar.getChildren().addAll(brand, sub);
        sidebar.getChildren().add(navButton("Dashboard", true,
                () -> new DashboardView(employeeService, dashboardService).build()));
        sidebar.getChildren().add(navButton("Employees", false,
                () -> new EmployeeListView(employeeService, auditLogService).build()));
        sidebar.getChildren().add(navButton("Attendance", false,
                () -> new AttendanceView(employeeService, attendanceService).build()));
        sidebar.getChildren().add(navButton("Leave", false,
                () -> new LeaveView(employeeService, leaveService, admin).build()));
        sidebar.getChildren().add(navButton("Salary", false,
                () -> new SalaryView(employeeService, salaryService).build()));
        sidebar.getChildren().add(navButton("Performance", false,
                () -> new PerformanceView(employeeService, performanceService).build()));
        sidebar.getChildren().add(navButton("Reports", false,
                () -> new ReportsView(employeeService, leaveService, reportService, auditLogService).build()));
        sidebar.getChildren().add(navButton("Data (Save/Load/Backup)", false,
                () -> new DataView(employeeService, leaveService, salaryService, performanceService,
                        fileStorageService, admin, auditLogService).build()));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logout = new Button("Logout");
        logout.getStyleClass().add("nav-button");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> { admin.logout(); app.showLogin(); });
        sidebar.getChildren().add(logout);

        return sidebar;
    }

    private Button navButton(String label, boolean selected, java.util.function.Supplier<Parent> viewSupplier) {
        Button button = new Button(label);
        button.getStyleClass().add("nav-button");
        if (selected) {
            button.getStyleClass().add("nav-button-selected");
            selectedNavButton = button;
        }
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {
            if (selectedNavButton != null) {
                selectedNavButton.getStyleClass().remove("nav-button-selected");
            }
            button.getStyleClass().add("nav-button-selected");
            selectedNavButton = button;
            showView(label, viewSupplier.get());
        });
        return button;
    }

    private HBox buildTopBar(String initialTitle) {
        HBox bar = new HBox();
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(initialTitle);
        title.getStyleClass().add("top-bar-title");
        title.setId("top-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label user = new Label("Signed in as " + admin.getUsername());
        user.getStyleClass().add("user-badge");

        bar.getChildren().addAll(title, spacer, user);
        return bar;
    }

    private void showView(String title, Parent view) {
        ((Label) root.getTop().lookup("#top-bar-title")).setText(title);
        VBox wrapper = new VBox(view);
        wrapper.getStyleClass().add("content-area");
        contentScroll.setContent(wrapper);
    }
}

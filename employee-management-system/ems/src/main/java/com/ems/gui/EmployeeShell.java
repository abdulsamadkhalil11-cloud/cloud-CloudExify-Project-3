package com.ems.gui;

import com.ems.exceptions.InvalidCredentialsException;
import com.ems.model.Employee;
import com.ems.model.LeaveRequest;
import com.ems.model.enums.LeaveType;
import com.ems.service.AuthService;
import com.ems.service.LeaveService;
import com.ems.service.SalaryService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

/** Self-service shell for an employee login: profile, attendance, leave, salary slip, and password change. */
public class EmployeeShell {

    private final MainApp app;
    private final Employee employee;
    private final LeaveService leaveService;
    private final SalaryService salaryService;
    private final AuthService authService;

    public EmployeeShell(MainApp app, Employee employee, LeaveService leaveService,
                          SalaryService salaryService, AuthService authService) {
        this.app = app;
        this.employee = employee;
        this.leaveService = leaveService;
        this.salaryService = salaryService;
        this.authService = authService;
    }

    public Parent build() {
        BorderPane root = new BorderPane();

        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Welcome, " + employee.getFullName());
        title.getStyleClass().add("top-bar-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Logout");
        logout.getStyleClass().add("btn-secondary");
        logout.setOnAction(e -> { employee.logout(); app.showLogin(); });
        topBar.getChildren().addAll(title, spacer, logout);
        root.setTop(topBar);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("My Profile", profileTab()));
        tabs.getTabs().add(new Tab("Attendance", attendanceTab()));
        tabs.getTabs().add(new Tab("Leave", leaveTab()));
        tabs.getTabs().add(new Tab("Salary Slip", salaryTab()));
        tabs.getTabs().add(new Tab("Settings", settingsTab()));

        VBox wrapper = new VBox(tabs);
        wrapper.getStyleClass().add("content-area");
        VBox.setVgrow(tabs, Priority.ALWAYS);
        root.setCenter(wrapper);

        if (employee.isBirthdayToday()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Happy Birthday, " + employee.getFullName() + "!");
            alert.setHeaderText(null);
            alert.show();
        }
        return root;
    }

    private VBox card(javafx.scene.Node... children) {
        VBox card = new VBox(10, children);
        card.getStyleClass().add("section-card");
        card.setPadding(new Insets(4));
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(20));
        return wrapper;
    }

    private VBox profileTab() {
        TextArea area = new TextArea(employee.generateEmployeeReport());
        area.setEditable(false);
        area.setPrefRowCount(10);
        area.setStyle("-fx-font-family: 'Consolas', monospace;");
        Label achievements = new Label("Achievements: " + employee.getAchievements());
        achievements.setWrapText(true);
        Label years = new Label("Years to retirement: " + employee.getYearsToRetirement()
                + "  |  Experience: " + employee.getExperienceYears() + " yrs");
        return card(new Label("Profile"), area, years, achievements);
    }

    private VBox attendanceTab() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefRowCount(10);
        area.setStyle("-fx-font-family: 'Consolas', monospace;");
        refreshAttendance(area);

        Button checkIn = new Button("Check In");
        checkIn.getStyleClass().add("btn-primary");
        checkIn.setOnAction(e -> { employee.markAttendance(LocalTime.now()); refreshAttendance(area); });
        Button checkOut = new Button("Check Out");
        checkOut.getStyleClass().add("btn-secondary");
        checkOut.setOnAction(e -> { employee.markCheckOut(LocalTime.now()); refreshAttendance(area); });

        HBox buttons = new HBox(10, checkIn, checkOut);
        return card(new Label("My Attendance"), buttons, area);
    }

    private void refreshAttendance(TextArea area) {
        StringBuilder sb = new StringBuilder("Attendance: " + employee.getAttendancePercentage() + "%\n\n");
        employee.viewAttendance().forEach(a -> sb.append(a).append('\n'));
        area.setText(sb.toString());
    }

    private VBox leaveTab() {
        ComboBox<LeaveType> type = new ComboBox<>();
        type.getItems().addAll(LeaveType.values());
        type.getSelectionModel().selectFirst();
        DatePicker start = new DatePicker(LocalDate.now());
        DatePicker end = new DatePicker(LocalDate.now().plusDays(1));
        TextField reason = new TextField();
        reason.setPromptText("Reason");

        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefRowCount(10);
        area.setStyle("-fx-font-family: 'Consolas', monospace;");
        refreshLeave(area);

        Button apply = new Button("Apply");
        apply.getStyleClass().add("btn-primary");
        apply.setOnAction(e -> {
            leaveService.applyLeave(employee.getEmployeeId(), type.getValue(), start.getValue(), end.getValue(), reason.getText().trim());
            refreshLeave(area);
        });

        HBox row = new HBox(10, type, start, end, reason, apply);
        row.setAlignment(Pos.CENTER_LEFT);
        return card(new Label("Apply / View Leave"), row, area);
    }

    private void refreshLeave(TextArea area) {
        StringBuilder sb = new StringBuilder();
        leaveService.getHistory(employee.getEmployeeId()).forEach(r -> sb.append(r).append('\n'));
        sb.append('\n');
        for (LeaveType t : LeaveType.values()) {
            sb.append(t).append(" remaining: ").append(leaveService.getRemainingLeaves(employee.getEmployeeId(), t)).append(" days\n");
        }
        area.setText(sb.toString());
    }

    private VBox salaryTab() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefRowCount(10);
        area.setStyle("-fx-font-family: 'Consolas', monospace;");
        Button view = new Button("View This Month's Slip");
        view.getStyleClass().add("btn-primary");
        view.setOnAction(e -> area.setText(salaryService.generateSalarySlip(employee, YearMonth.now(), 0, 0).toString()));
        return card(new Label("Salary Slip"), view, area);
    }

    private VBox settingsTab() {
        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Current password");
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New password");
        Label msg = new Label();
        msg.getStyleClass().add("error-text");
        msg.setWrapText(true);

        Button change = new Button("Change Password");
        change.getStyleClass().add("btn-primary");
        change.setOnAction(e -> {
            try {
                authService.changePassword(employee, oldPass.getText(), newPass.getText());
                msg.setStyle("-fx-text-fill: #16a34a;");
                msg.setText("Password updated.");
            } catch (InvalidCredentialsException ex) {
                msg.setStyle("-fx-text-fill: #dc2626;");
                msg.setText(ex.getMessage());
            }
        });

        return card(new Label("Change Password"), oldPass, newPass, change, msg);
    }
}

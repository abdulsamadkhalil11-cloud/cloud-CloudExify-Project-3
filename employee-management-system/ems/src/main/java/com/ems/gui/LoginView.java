package com.ems.gui;

import com.ems.exceptions.InvalidCredentialsException;
import com.ems.model.Employee;
import com.ems.model.Admin;
import com.ems.service.AuthService;
import com.ems.service.EmployeeService;
import com.ems.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/** Login screen: Admin and Employee login side by side in tabs, each with a 3-attempt limit. */
public class LoginView {

    private final MainApp app;
    private final Admin admin;
    private final EmployeeService employeeService;
    private final AuthService authService;

    private int adminAttempts = 0;
    private int employeeAttempts = 0;

    public LoginView(MainApp app, Admin admin, EmployeeService employeeService, AuthService authService) {
        this.app = app;
        this.admin = admin;
        this.employeeService = employeeService;
        this.authService = authService;
    }

    public Parent build() {
        HBox root = new HBox();
        root.getChildren().addAll(buildBrandPanel(), buildFormPanel());
        HBox.setHgrow(root.getChildren().get(1), Priority.ALWAYS);
        return root;
    }

    private VBox buildBrandPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(420);
        panel.setMinWidth(320);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(40));
        panel.setStyle("-fx-background-color: #1e2a3a;");

        Label title = new Label("Employee Management System");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);

        Label subtitle = new Label("Java OOP Portfolio Project - Inheritance, Polymorphism,\n"
                + "Encapsulation & Abstraction demonstrated end to end.");
        subtitle.setTextFill(Color.web("#8fa1b8"));
        subtitle.setWrapText(true);
        subtitle.setFont(Font.font("Segoe UI", 13));

        panel.getChildren().addAll(title, subtitle);
        return panel;
    }

    private StackPane buildFormPanel() {
        StackPane wrapper = new StackPane();
        wrapper.setStyle("-fx-background-color: #f4f6f9;");

        TabPane tabs = new TabPane();
        tabs.setMaxWidth(360);
        tabs.setMaxHeight(360);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Admin Login", buildAdminForm()));
        tabs.getTabs().add(new Tab("Employee Login", buildEmployeeForm()));

        wrapper.getChildren().add(tabs);
        StackPane.setAlignment(tabs, Pos.CENTER);
        return wrapper;
    }

    private VBox buildAdminForm() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(24));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        Label error = new Label();
        error.getStyleClass().add("error-text");
        error.setWrapText(true);

        Button loginBtn = new Button("Log In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Label hint = new Label("Demo login: admin / Admin@123");
        hint.setStyle("-fx-text-fill: #8a96a3; -fx-font-size: 11px;");

        loginBtn.setOnAction(e -> {
            if (adminAttempts >= Constants.MAX_LOGIN_ATTEMPTS) {
                error.setText("Too many failed attempts. Restart the app to try again.");
                return;
            }
            try {
                admin.login(username.getText().trim(), password.getText());
                app.showAdminShell();
            } catch (InvalidCredentialsException ex) {
                adminAttempts++;
                int remaining = Constants.MAX_LOGIN_ATTEMPTS - adminAttempts;
                error.setText(remaining > 0
                        ? "Invalid credentials. " + remaining + " attempt(s) remaining."
                        : "Too many failed attempts. Restart the app to try again.");
                if (remaining <= 0) {
                    loginBtn.setDisable(true);
                }
            }
        });

        box.getChildren().addAll(new Label("Admin Sign In"), username, password, loginBtn, error, hint);
        box.getChildren().get(0).setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e2a3a;");
        return box;
    }

    private VBox buildEmployeeForm() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(24));

        TextField employeeId = new TextField();
        employeeId.setPromptText("Employee ID (e.g. DEV-0001)");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        Label error = new Label();
        error.getStyleClass().add("error-text");
        error.setWrapText(true);

        Button loginBtn = new Button("Log In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Hyperlink forgot = new Hyperlink("Forgot password?");

        Label hint = new Label("Demo login: DEV-0001 / Dev@1234");
        hint.setStyle("-fx-text-fill: #8a96a3; -fx-font-size: 11px;");

        loginBtn.setOnAction(e -> {
            if (employeeAttempts >= Constants.MAX_LOGIN_ATTEMPTS) {
                error.setText("Too many failed attempts. Try 'Forgot password?' below.");
                return;
            }
            try {
                Employee employee = authService.authenticateEmployee(
                        employeeService.getAll(), employeeId.getText().trim(), password.getText());
                app.showEmployeeShell(employee);
            } catch (InvalidCredentialsException ex) {
                employeeAttempts++;
                int remaining = Constants.MAX_LOGIN_ATTEMPTS - employeeAttempts;
                error.setText(remaining > 0
                        ? "Invalid credentials. " + remaining + " attempt(s) remaining."
                        : "Too many failed attempts. Try 'Forgot password?' below.");
            }
        });

        forgot.setOnAction(e -> showForgotPasswordDialog(employeeId.getText().trim(), error));

        box.getChildren().addAll(new Label("Employee Sign In"), employeeId, password, loginBtn, forgot, error, hint);
        box.getChildren().get(0).setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e2a3a;");
        return box;
    }

    private void showForgotPasswordDialog(String prefilledId, Label errorLabel) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Verify your identity with your CNIC on file.");

        TextField idField = new TextField(prefilledId);
        idField.setPromptText("Employee ID");
        TextField cnicField = new TextField();
        cnicField.setPromptText("CNIC (00000-0000000-0)");

        VBox content = new VBox(10, new Label("Employee ID:"), idField, new Label("CNIC:"), cnicField);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    Employee employee = employeeService.getById(idField.getText().trim());
                    String temp = authService.forgotPassword(employee, cnicField.getText().trim());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Identity verified. Temporary password: " + temp + "\nLog in and change it right away.");
                    alert.setHeaderText("Password Reset");
                    alert.showAndWait();
                } catch (Exception ex) {
                    errorLabel.setText("Could not verify identity: " + ex.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }
}

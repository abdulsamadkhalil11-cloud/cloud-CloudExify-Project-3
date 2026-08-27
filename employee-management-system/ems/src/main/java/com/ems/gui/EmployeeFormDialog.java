package com.ems.gui;

import com.ems.exceptions.EMSException;
import com.ems.model.*;
import com.ems.model.enums.Department;
import com.ems.model.enums.Gender;
import com.ems.service.EmployeeService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

/** Add/Edit employee modal. Role-specific fields (Manager/Developer/HR) show or hide based on the role picker. */
public final class EmployeeFormDialog {

    private EmployeeFormDialog() {
    }

    public static void showAddDialog(Stage owner, EmployeeService employeeService, Runnable onSuccess) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Add Employee");

        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("Manager", "Developer", "HR");
        role.setValue("Developer");

        TextField fullName = new TextField();
        Spinner<Integer> age = new Spinner<>(18, 65, 28);
        ComboBox<Gender> gender = new ComboBox<>(); gender.getItems().addAll(Gender.values()); gender.setValue(Gender.MALE);
        TextField cnic = new TextField(); cnic.setPromptText("00000-0000000-0");
        TextField phone = new TextField(); phone.setPromptText("+923001234567");
        TextField email = new TextField();
        TextField address = new TextField();
        ComboBox<Department> department = new ComboBox<>(); department.getItems().addAll(Department.values()); department.setValue(Department.ENGINEERING);
        TextField designation = new TextField();
        TextField salary = new TextField(); salary.setPromptText("e.g. 120000");
        DatePicker joiningDate = new DatePicker(LocalDate.now());
        DatePicker dob = new DatePicker(LocalDate.now().minusYears(28));
        TextField username = new TextField();
        PasswordField password = new PasswordField(); password.setPromptText("min 8 chars, letter+digit+symbol");

        // Manager-only
        TextField teamSize = new TextField("5");
        TextField budget = new TextField("100000");
        // Developer-only
        TextField language = new TextField("Java");
        TextField framework = new TextField("Spring Boot");
        TextField github = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int r = 0;
        grid.addRow(r++, new Label("Role:"), role);
        grid.addRow(r++, new Label("Full name:"), fullName);
        grid.addRow(r++, new Label("Age:"), age, new Label("Gender:"), gender);
        grid.addRow(r++, new Label("CNIC:"), cnic);
        grid.addRow(r++, new Label("Phone:"), phone);
        grid.addRow(r++, new Label("Email:"), email);
        grid.addRow(r++, new Label("Address:"), address);
        grid.addRow(r++, new Label("Department:"), department, new Label("Designation:"), designation);
        grid.addRow(r++, new Label("Salary:"), salary);
        grid.addRow(r++, new Label("Joining date:"), joiningDate, new Label("Date of birth:"), dob);
        grid.addRow(r++, new Label("Username:"), username);
        grid.addRow(r++, new Label("Password:"), password);

        VBox managerFields = new VBox(6, new Label("Team size:"), teamSize, new Label("Department budget:"), budget);
        VBox developerFields = new VBox(6, new Label("Programming language:"), language,
                new Label("Framework:"), framework, new Label("GitHub username:"), github);
        VBox roleSpecificHolder = new VBox(developerFields);

        role.valueProperty().addListener((obs, oldV, newV) -> {
            roleSpecificHolder.getChildren().clear();
            if ("Manager".equals(newV)) roleSpecificHolder.getChildren().add(managerFields);
            else if ("Developer".equals(newV)) roleSpecificHolder.getChildren().add(developerFields);
        });

        Label error = new Label();
        error.getStyleClass().add("error-text");
        error.setWrapText(true);

        Button submit = new Button("Add Employee");
        submit.getStyleClass().add("btn-primary");
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> dialog.close());

        submit.setOnAction(e -> {
            try {
                double salaryValue = Double.parseDouble(salary.getText().trim());
                String id = employeeService.generateNextId(
                        role.getValue().equals("Manager") ? "MGR" : role.getValue().equals("Developer") ? "DEV" : "HR");
                Employee employee;
                switch (role.getValue()) {
                    case "Manager":
                        employee = new Manager(id, fullName.getText().trim(), age.getValue(), gender.getValue(),
                                cnic.getText().trim(), phone.getText().trim(), email.getText().trim(),
                                address.getText().trim(), department.getValue(), designation.getText().trim(),
                                salaryValue, joiningDate.getValue(), dob.getValue(), username.getText().trim(),
                                password.getText(), Integer.parseInt(teamSize.getText().trim()),
                                Double.parseDouble(budget.getText().trim()));
                        break;
                    case "Developer":
                        employee = new Developer(id, fullName.getText().trim(), age.getValue(), gender.getValue(),
                                cnic.getText().trim(), phone.getText().trim(), email.getText().trim(),
                                address.getText().trim(), department.getValue(), designation.getText().trim(),
                                salaryValue, joiningDate.getValue(), dob.getValue(), username.getText().trim(),
                                password.getText(), language.getText().trim(), framework.getText().trim(),
                                github.getText().trim());
                        break;
                    default:
                        employee = new HR(id, fullName.getText().trim(), age.getValue(), gender.getValue(),
                                cnic.getText().trim(), phone.getText().trim(), email.getText().trim(),
                                address.getText().trim(), department.getValue(), designation.getText().trim(),
                                salaryValue, joiningDate.getValue(), dob.getValue(), username.getText().trim(),
                                password.getText());
                }
                employeeService.addEmployee(employee);
                dialog.close();
                onSuccess.run();
            } catch (NumberFormatException nfe) {
                error.setText("Salary/team size/budget must be numbers.");
            } catch (EMSException ex) {
                error.setText(ex.getMessage());
            }
        });

        HBox buttons = new HBox(10, submit, cancel);
        VBox content = new VBox(14, grid, roleSpecificHolder, error, buttons);
        content.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        dialog.setScene(new javafx.scene.Scene(scroll, 560, 640));
        dialog.getScene().getStylesheets().add(EmployeeFormDialog.class.getResource("/css/theme.css").toExternalForm());
        dialog.showAndWait();
    }

    public static void showEditDialog(Stage owner, Employee employee, Runnable onSuccess) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Edit Employee - " + employee.getEmployeeId());

        TextField phone = new TextField(employee.getPhoneNumber());
        TextField email = new TextField(employee.getEmail());
        TextField address = new TextField(employee.getAddress());
        TextField designation = new TextField(employee.getDesignation());
        TextField salary = new TextField(String.valueOf(employee.getSalary()));
        ComboBox<com.ems.model.enums.EmploymentStatus> status = new ComboBox<>();
        status.getItems().addAll(com.ems.model.enums.EmploymentStatus.values());
        status.setValue(employee.getEmploymentStatus());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int r = 0;
        grid.addRow(r++, new Label("Phone:"), phone);
        grid.addRow(r++, new Label("Email:"), email);
        grid.addRow(r++, new Label("Address:"), address);
        grid.addRow(r++, new Label("Designation:"), designation);
        grid.addRow(r++, new Label("Base salary:"), salary);
        grid.addRow(r++, new Label("Status:"), status);

        Label error = new Label();
        error.getStyleClass().add("error-text");
        error.setWrapText(true);

        Button submit = new Button("Save Changes");
        submit.getStyleClass().add("btn-primary");
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> dialog.close());

        submit.setOnAction(e -> {
            try {
                employee.updateProfile(phone.getText().trim(), email.getText().trim(),
                        address.getText().trim(), designation.getText().trim());
                employee.setSalary(Double.parseDouble(salary.getText().trim()));
                employee.setEmploymentStatus(status.getValue());
                dialog.close();
                onSuccess.run();
            } catch (NumberFormatException nfe) {
                error.setText("Salary must be a number.");
            } catch (EMSException ex) {
                error.setText(ex.getMessage());
            }
        });

        HBox buttons = new HBox(10, submit, cancel);
        VBox content = new VBox(14, grid, error, buttons);
        content.setPadding(new Insets(20));
        dialog.setScene(new javafx.scene.Scene(content, 480, 420));
        dialog.getScene().getStylesheets().add(EmployeeFormDialog.class.getResource("/css/theme.css").toExternalForm());
        dialog.showAndWait();
    }
}

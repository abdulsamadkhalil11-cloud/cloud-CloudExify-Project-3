package com.ems.gui.views;

import com.ems.gui.EmployeeFormDialog;
import com.ems.model.Employee;
import com.ems.service.AuditLogService;
import com.ems.service.EmployeeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/** Employee roster: search box, sortable table, and Add/Edit/Remove actions. */
public class EmployeeListView {

    private final EmployeeService employeeService;
    private final AuditLogService auditLogService;
    private final TableView<Employee> table = new TableView<>();
    private VBox root;

    public EmployeeListView(EmployeeService employeeService, AuditLogService auditLogService) {
        this.employeeService = employeeService;
        this.auditLogService = auditLogService;
    }

    public Parent build() {
        root = new VBox(16);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField search = new TextField();
        search.setPromptText("Search by ID, name, email, or phone...");
        search.setPrefWidth(320);
        search.textProperty().addListener((obs, old, val) ->
                refresh(val == null || val.isBlank() ? employeeService.getAll() : employeeService.search(val)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Employee");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> EmployeeFormDialog.showAddDialog(currentStage(), employeeService,
                () -> refresh(employeeService.getAll())));

        toolbar.getChildren().addAll(search, spacer, addBtn);

        buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button editBtn = new Button("Edit Selected");
        editBtn.getStyleClass().add("btn-secondary");
        editBtn.setOnAction(e -> {
            Employee selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                EmployeeFormDialog.showEditDialog(currentStage(), selected, () -> refresh(employeeService.getAll()));
            }
        });
        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("btn-danger");
        removeBtn.setOnAction(e -> {
            Employee selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Remove " + selected.getFullName() + " (" + selected.getEmployeeId() + ")?");
            confirm.showAndWait().filter(bt -> bt == ButtonType.OK).ifPresent(bt -> {
                try {
                    employeeService.removeEmployee(selected.getEmployeeId());
                    auditLogService.logQuiet("Removed employee " + selected.getEmployeeId() + " via GUI");
                    refresh(employeeService.getAll());
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                }
            });
        });
        actions.getChildren().addAll(editBtn, removeBtn);

        VBox card = new VBox(14, toolbar, table, actions);
        card.getStyleClass().add("section-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        root.getChildren().add(card);
        VBox.setVgrow(root, Priority.ALWAYS);
        refresh(employeeService.getAll());
        return root;
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<Employee, String> idCol = column("ID", "employeeId", 90);
        TableColumn<Employee, String> nameCol = column("Name", "fullName", 150);
        TableColumn<Employee, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClass().getSimpleName()));
        roleCol.setPrefWidth(90);
        TableColumn<Employee, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDepartment())));
        deptCol.setPrefWidth(130);
        TableColumn<Employee, String> designationCol = column("Designation", "designation", 150);
        TableColumn<Employee, String> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f", data.getValue().calculateSalary())));
        salaryCol.setPrefWidth(100);
        TableColumn<Employee, String> attendanceCol = new TableColumn<>("Attendance");
        attendanceCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.0f%%", data.getValue().getAttendancePercentage())));
        attendanceCol.setPrefWidth(90);
        TableColumn<Employee, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getEmploymentStatus())));
        statusCol.setPrefWidth(90);

        table.getColumns().addAll(List.of(idCol, nameCol, roleCol, deptCol, designationCol, salaryCol, attendanceCol, statusCol));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(420);
    }

    private TableColumn<Employee, String> column(String title, String property, double width) {
        TableColumn<Employee, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    private void refresh(List<Employee> employees) {
        ObservableList<Employee> data = FXCollections.observableArrayList(employees);
        table.setItems(data);
    }

    private Stage currentStage() {
        return (Stage) root.getScene().getWindow();
    }
}

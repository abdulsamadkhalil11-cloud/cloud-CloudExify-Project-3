package com.ems.gui.views;

import com.ems.model.Admin;
import com.ems.model.Employee;
import com.ems.model.LeaveRequest;
import com.ems.model.enums.LeaveType;
import com.ems.service.EmployeeService;
import com.ems.service.LeaveService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;

/** Leave panel: apply on behalf of an employee, review pending requests, check balances. */
public class LeaveView {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final Admin admin;
    private final ListView<LeaveRequest> pendingList = new ListView<>();
    private final TextArea detail = new TextArea();

    public LeaveView(EmployeeService employeeService, LeaveService leaveService, Admin admin) {
        this.employeeService = employeeService;
        this.leaveService = leaveService;
        this.admin = admin;
    }

    public Parent build() {
        VBox root = new VBox(16);

        VBox applyCard = new VBox(10);
        applyCard.getStyleClass().add("section-card");
        Label applyTitle = new Label("Apply Leave");
        applyTitle.getStyleClass().add("section-title");

        ComboBox<Employee> employeePicker = new ComboBox<>(FXCollections.observableArrayList(employeeService.getAll()));
        employeePicker.setConverter(nameConverter());
        if (!employeePicker.getItems().isEmpty()) employeePicker.getSelectionModel().selectFirst();

        ComboBox<LeaveType> typePicker = new ComboBox<>(FXCollections.observableArrayList(LeaveType.values()));
        typePicker.getSelectionModel().selectFirst();
        DatePicker start = new DatePicker(LocalDate.now());
        DatePicker end = new DatePicker(LocalDate.now().plusDays(1));
        TextField reason = new TextField();
        reason.setPromptText("Reason");
        reason.setPrefWidth(220);

        Button applyBtn = new Button("Submit Application");
        applyBtn.getStyleClass().add("btn-primary");
        Label applyMsg = new Label();

        applyBtn.setOnAction(e -> {
            Employee emp = employeePicker.getValue();
            if (emp == null) return;
            LeaveRequest r = leaveService.applyLeave(emp.getEmployeeId(), typePicker.getValue(),
                    start.getValue(), end.getValue(), reason.getText().trim());
            applyMsg.setText("Submitted: " + r.getRequestId());
            refreshPending();
        });

        HBox applyRow1 = new HBox(10, new Label("Employee:"), employeePicker, new Label("Type:"), typePicker);
        applyRow1.setAlignment(Pos.CENTER_LEFT);
        HBox applyRow2 = new HBox(10, new Label("Start:"), start, new Label("End:"), end, reason, applyBtn);
        applyRow2.setAlignment(Pos.CENTER_LEFT);
        applyCard.getChildren().addAll(applyTitle, applyRow1, applyRow2, applyMsg);

        VBox pendingCard = new VBox(10);
        pendingCard.getStyleClass().add("section-card");
        Label pendingTitle = new Label("Pending Requests");
        pendingTitle.getStyleClass().add("section-title");
        pendingList.setPrefHeight(160);
        pendingList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(LeaveRequest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });

        HBox decisionButtons = new HBox(10);
        Button approveBtn = new Button("Approve Selected");
        approveBtn.getStyleClass().add("btn-primary");
        Button rejectBtn = new Button("Reject Selected");
        rejectBtn.getStyleClass().add("btn-danger");
        approveBtn.setOnAction(e -> decide(true));
        rejectBtn.setOnAction(e -> decide(false));
        decisionButtons.getChildren().addAll(approveBtn, rejectBtn);

        pendingCard.getChildren().addAll(pendingTitle, pendingList, decisionButtons);

        VBox historyCard = new VBox(10);
        historyCard.getStyleClass().add("section-card");
        Label historyTitle = new Label("History / Remaining Balance");
        historyTitle.getStyleClass().add("section-title");
        Button historyBtn = new Button("Show for Selected Employee (above)");
        historyBtn.getStyleClass().add("btn-secondary");
        historyBtn.setOnAction(e -> {
            Employee emp = employeePicker.getValue();
            if (emp == null) return;
            StringBuilder sb = new StringBuilder();
            leaveService.getHistory(emp.getEmployeeId()).forEach(r -> sb.append(r).append('\n'));
            sb.append('\n');
            for (LeaveType t : LeaveType.values()) {
                sb.append(t).append(" remaining: ").append(leaveService.getRemainingLeaves(emp.getEmployeeId(), t)).append(" days\n");
            }
            detail.setText(sb.toString());
        });
        detail.setEditable(false);
        detail.setPrefRowCount(8);
        detail.setStyle("-fx-font-family: 'Consolas', monospace;");
        historyCard.getChildren().addAll(historyTitle, historyBtn, detail);

        root.getChildren().addAll(applyCard, pendingCard, historyCard);
        refreshPending();
        return root;
    }

    private void decide(boolean approve) {
        LeaveRequest selected = pendingList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (approve) {
            leaveService.approve(selected, admin.getUsername());
        } else {
            leaveService.reject(selected, admin.getUsername());
        }
        refreshPending();
    }

    private void refreshPending() {
        pendingList.setItems(FXCollections.observableArrayList(leaveService.getPendingRequests()));
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

package com.ems.gui.views;

import com.ems.model.Attendance;
import com.ems.model.Employee;
import com.ems.service.AttendanceService;
import com.ems.service.EmployeeService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

/** Attendance panel: pick an employee, check in/out, and view their record or the roster's late list. */
public class AttendanceView {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final TextArea output = new TextArea();
    private final ComboBox<Employee> employeePicker = new ComboBox<>();

    public AttendanceView(EmployeeService employeeService, AttendanceService attendanceService) {
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
    }

    public Parent build() {
        VBox card = new VBox(14);
        card.getStyleClass().add("section-card");

        employeePicker.getItems().addAll(employeeService.getAll());
        employeePicker.setConverter(nameConverter());
        employeePicker.setPrefWidth(260);
        if (!employeePicker.getItems().isEmpty()) {
            employeePicker.getSelectionModel().selectFirst();
        }

        Button checkInBtn = new Button("Check In");
        checkInBtn.getStyleClass().add("btn-primary");
        checkInBtn.setOnAction(e -> withSelected(emp -> {
            attendanceService.checkIn(emp);
            output.setText(emp.getFullName() + " checked in at " + LocalTime.now().withNano(0));
        }));

        Button checkOutBtn = new Button("Check Out");
        checkOutBtn.getStyleClass().add("btn-secondary");
        checkOutBtn.setOnAction(e -> withSelected(emp -> {
            attendanceService.checkOut(emp);
            output.setText(emp.getFullName() + " checked out at " + LocalTime.now().withNano(0));
        }));

        Button viewBtn = new Button("View Full History");
        viewBtn.getStyleClass().add("btn-secondary");
        viewBtn.setOnAction(e -> withSelected(emp -> {
            StringBuilder sb = new StringBuilder(emp.getFullName() + " - " + emp.getAttendancePercentage() + "% attendance\n\n");
            emp.viewAttendance().forEach(a -> sb.append(a).append('\n'));
            output.setText(sb.toString());
        }));

        Button monthlyBtn = new Button("This Month's Report");
        monthlyBtn.getStyleClass().add("btn-secondary");
        monthlyBtn.setOnAction(e -> withSelected(emp ->
                output.setText(attendanceService.getMonthlyReport(emp, YearMonth.now()))));

        Button lateBtn = new Button("Late Arrivals Today (all staff)");
        lateBtn.getStyleClass().add("btn-secondary");
        lateBtn.setOnAction(e -> {
            List<Employee> late = attendanceService.getLateArrivalsToday(employeeService.getAll());
            StringBuilder sb = new StringBuilder("Late arrivals today:\n\n");
            if (late.isEmpty()) sb.append("(none)");
            late.forEach(emp -> sb.append("  ").append(emp).append('\n'));
            output.setText(sb.toString());
        });

        HBox buttons = new HBox(10, checkInBtn, checkOutBtn, viewBtn, monthlyBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        output.setEditable(false);
        output.setPrefRowCount(14);
        output.setStyle("-fx-font-family: 'Consolas', monospace;");
        VBox.setVgrow(output, Priority.ALWAYS);

        card.getChildren().addAll(new Label("Employee:"), employeePicker, buttons, lateBtn, output);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private void withSelected(java.util.function.Consumer<Employee> action) {
        Employee selected = employeePicker.getSelectionModel().getSelectedItem();
        if (selected != null) {
            action.accept(selected);
        }
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

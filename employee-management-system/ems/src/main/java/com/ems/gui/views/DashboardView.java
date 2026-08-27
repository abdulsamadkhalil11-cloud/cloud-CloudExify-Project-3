package com.ems.gui.views;

import com.ems.model.Developer;
import com.ems.model.Employee;
import com.ems.model.HR;
import com.ems.model.Manager;
import com.ems.model.enums.EmploymentStatus;
import com.ems.service.DashboardService;
import com.ems.service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Dashboard: statistic cards, a department pie chart, and a top-performers list. */
public class DashboardView {

    private final EmployeeService employeeService;
    private final DashboardService dashboardService;

    public DashboardView(EmployeeService employeeService, DashboardService dashboardService) {
        this.employeeService = employeeService;
        this.dashboardService = dashboardService;
    }

    public Parent build() {
        VBox root = new VBox(20);
        List<Employee> roster = employeeService.getAll();

        root.getChildren().add(buildStatCards(roster));

        HBox middle = new HBox(20);
        middle.getChildren().addAll(buildDepartmentChart(roster), buildTopPerformers(roster));
        HBox.setHgrow(middle.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(middle.getChildren().get(1), Priority.ALWAYS);
        root.getChildren().add(middle);

        return root;
    }

    private GridPane buildStatCards(List<Employee> roster) {
        long managers = roster.stream().filter(e -> e instanceof Manager).count();
        long developers = roster.stream().filter(e -> e instanceof Developer).count();
        long hrStaff = roster.stream().filter(e -> e instanceof HR).count();
        long active = roster.stream().filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE).count();
        double totalExpense = roster.stream().mapToDouble(Employee::calculateSalary).sum();
        double avgAttendance = roster.stream().mapToDouble(Employee::getAttendancePercentage).average().orElse(0);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(25);
            grid.getColumnConstraints().add(c);
        }
        grid.add(statCard("Total Employees", String.valueOf(roster.size())), 0, 0);
        grid.add(statCard("Managers / Devs / HR", managers + " / " + developers + " / " + hrStaff), 1, 0);
        grid.add(statCard("Active", active + " / " + roster.size()), 2, 0);
        grid.add(statCard("Avg. Attendance", String.format("%.1f%%", avgAttendance)), 3, 0);
        grid.add(statCard("Total Monthly Salary Expense", String.format("Rs. %,.0f", totalExpense)), 0, 1, 2, 1);
        Employee latest = roster.stream().max(Comparator.comparing(Employee::getJoiningDate)).orElse(null);
        grid.add(statCard("Latest Hire", latest == null ? "-" : latest.getFullName()), 2, 1, 2, 1);
        return grid;
    }

    private VBox statCard(String label, String value) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-card-value");
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-card-label");
        card.getChildren().addAll(valueLabel, textLabel);
        return card;
    }

    private VBox buildDepartmentChart(List<Employee> roster) {
        VBox box = new VBox(10);
        box.getStyleClass().add("section-card");
        Label title = new Label("Headcount by Department");
        title.getStyleClass().add("section-title");

        Map<String, Long> breakdown = dashboardService.departmentBreakdown(roster);
        PieChart chart = new PieChart(FXCollections.observableArrayList(
                breakdown.entrySet().stream()
                        .map(e -> new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()))
                        .toList()));
        chart.setLegendVisible(true);
        chart.setLabelsVisible(false);
        chart.setPrefHeight(260);

        box.getChildren().addAll(title, chart);
        return box;
    }

    private VBox buildTopPerformers(List<Employee> roster) {
        VBox box = new VBox(10);
        box.getStyleClass().add("section-card");
        Label title = new Label("Top Performers");
        title.getStyleClass().add("section-title");
        box.getChildren().add(title);

        roster.stream()
                .sorted(Comparator.comparingDouble(Employee::getPerformanceScore).reversed())
                .limit(5)
                .forEach(e -> {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(6, 0, 6, 0));
                    Label name = new Label(e.getFullName());
                    name.setPrefWidth(160);
                    Label score = new Label(String.format("%.1f", e.getPerformanceScore()));
                    score.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");
                    row.getChildren().addAll(name, score);
                    box.getChildren().add(row);
                });
        return box;
    }
}

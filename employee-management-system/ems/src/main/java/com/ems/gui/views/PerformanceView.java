package com.ems.gui.views;

import com.ems.model.Employee;
import com.ems.service.EmployeeService;
import com.ems.service.PerformanceService;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Performance panel: submit a review, then see the ranking with a promotion flag. */
public class PerformanceView {

    private final EmployeeService employeeService;
    private final PerformanceService performanceService;
    private final TextArea output = new TextArea();

    public PerformanceView(EmployeeService employeeService, PerformanceService performanceService) {
        this.employeeService = employeeService;
        this.performanceService = performanceService;
    }

    public Parent build() {
        VBox root = new VBox(16);

        VBox reviewCard = new VBox(10);
        reviewCard.getStyleClass().add("section-card");
        Label reviewTitle = new Label("Add Performance Review");
        reviewTitle.getStyleClass().add("section-title");

        ComboBox<Employee> picker = new ComboBox<>(FXCollections.observableArrayList(employeeService.getAll()));
        picker.setConverter(nameConverter());
        if (!picker.getItems().isEmpty()) picker.getSelectionModel().selectFirst();
        Slider ratingSlider = new Slider(0, 100, 80);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setPrefWidth(180);
        Label ratingValue = new Label("80");
        ratingSlider.valueProperty().addListener((obs, o, n) -> ratingValue.setText(String.valueOf(n.intValue())));
        TextField feedback = new TextField();
        feedback.setPromptText("Feedback");
        feedback.setPrefWidth(220);
        Button submitBtn = new Button("Submit Review");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setOnAction(e -> {
            Employee emp = picker.getValue();
            if (emp == null) return;
            performanceService.addReview(emp, ratingSlider.getValue(), feedback.getText().trim(), "admin");
            refreshRanking();
        });

        HBox reviewRow = new HBox(10, new Label("Employee:"), picker, new Label("Rating:"), ratingSlider,
                ratingValue, feedback, submitBtn);
        reviewRow.setAlignment(Pos.CENTER_LEFT);
        reviewCard.getChildren().addAll(reviewTitle, reviewRow);

        VBox rankingCard = new VBox(10);
        rankingCard.getStyleClass().add("section-card");
        Label rankingTitle = new Label("Ranking & Promotion Recommendation");
        rankingTitle.getStyleClass().add("section-title");
        output.setEditable(false);
        output.setPrefRowCount(12);
        output.setStyle("-fx-font-family: 'Consolas', monospace;");
        rankingCard.getChildren().addAll(rankingTitle, output);

        root.getChildren().addAll(reviewCard, rankingCard);
        refreshRanking();
        return root;
    }

    private void refreshRanking() {
        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (Employee e : performanceService.getRanking(employeeService.getAll())) {
            boolean promote = performanceService.recommendPromotion(e);
            sb.append(String.format("%2d. %-20s %5.1f  %s%n", rank++, e.getFullName(), e.getPerformanceScore(),
                    promote ? "<- promotion candidate" : ""));
        }
        output.setText(sb.toString());
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

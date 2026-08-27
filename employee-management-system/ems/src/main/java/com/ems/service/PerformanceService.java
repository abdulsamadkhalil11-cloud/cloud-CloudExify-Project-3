package com.ems.service;

import com.ems.model.Employee;
import com.ems.model.PerformanceReview;
import com.ems.util.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Performance reviews, ranking/top-performer views, and promotion recommendations. */
public class PerformanceService {

    private final List<PerformanceReview> reviews = new ArrayList<>();

    public void setAll(List<PerformanceReview> loaded) {
        reviews.clear();
        reviews.addAll(loaded);
    }

    public List<PerformanceReview> getAll() {
        return new ArrayList<>(reviews);
    }

    public PerformanceReview addReview(Employee employee, double rating, String feedback, String reviewerId) {
        PerformanceReview review = new PerformanceReview(employee.getEmployeeId(), rating, feedback, reviewerId);
        reviews.add(review);
        employee.setPerformanceScore(rating);
        return review;
    }

    public List<PerformanceReview> getHistory(String employeeId) {
        return reviews.stream().filter(r -> r.getEmployeeId().equals(employeeId)).collect(Collectors.toList());
    }

    public List<Employee> getRanking(List<Employee> roster) {
        List<Employee> ranked = new ArrayList<>(roster);
        ranked.sort(Comparator.comparingDouble(Employee::getPerformanceScore).reversed());
        return ranked;
    }

    public Employee getTopPerformer(List<Employee> roster) {
        return getRanking(roster).stream().findFirst().orElse(null);
    }

    public List<Employee> getLowestPerformers(List<Employee> roster, int count) {
        List<Employee> ranked = getRanking(roster);
        java.util.Collections.reverse(ranked);
        return ranked.stream().limit(count).collect(Collectors.toList());
    }

    public boolean recommendPromotion(Employee employee) {
        return employee.getPerformanceScore() >= Constants.PROMOTION_SCORE_THRESHOLD
                && employee.getExperienceYears() >= Constants.PROMOTION_MIN_EXPERIENCE_YEARS;
    }
}

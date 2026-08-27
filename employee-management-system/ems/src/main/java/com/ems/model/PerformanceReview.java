package com.ems.model;

import java.io.Serializable;
import java.time.LocalDate;

/** A single performance evaluation entry for an employee. */
public class PerformanceReview implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String employeeId;
    private final double rating;
    private final String feedback;
    private final String reviewedBy;
    private final LocalDate date;

    public PerformanceReview(String employeeId, double rating, String feedback, String reviewedBy) {
        this.employeeId = employeeId;
        this.rating = rating;
        this.feedback = feedback;
        this.reviewedBy = reviewedBy;
        this.date = LocalDate.now();
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public double getRating() {
        return rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("[%s] Rating: %.1f/100 | By: %s | %s | \"%s\"",
                date, rating, reviewedBy, employeeId, feedback);
    }
}

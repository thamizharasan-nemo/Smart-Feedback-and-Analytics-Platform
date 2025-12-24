package com.example.FeedbackSystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.LocalDate;

@Entity
@Data
@RequiredArgsConstructor

public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int feedbackId;

    @NotNull(message = "Rating is required")
    @Min(value = 1)
    @Max(value = 5)
    private int courseRating;

    @NotNull(message = "Rating is required")
    @Min(value = 1)
    @Max(value = 5)
    private int instructorRating;

    private String courseComment;
    private String instructorComment;

    private boolean anonymous;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDate submittedAt;

    @PrePersist
    // PrePersist used for methods in entity class. For to execute this method before -
    // - the entity inserted in the database.
    // ex: Date should be generated before storing the entity data in the database
    public void setSubmissionDate(){
        this.submittedAt = LocalDate.now();
    }
}

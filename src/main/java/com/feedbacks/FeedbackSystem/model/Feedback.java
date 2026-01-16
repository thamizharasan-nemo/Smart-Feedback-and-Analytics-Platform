package com.feedbacks.FeedbackSystem.model;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@RequiredArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_feedback_course",
                        columnList = "course_id"),
                @Index(name = "idx_feedback_instructor",
                        columnList = "instructor_id"
                ),
                @Index(name = "idx_feedback_submitted_at",
                        columnList = "submitted_at"
                )
        }
)
@SQLDelete(sql = "UPDATE feedback SET is_deleted = true WHERE feedback_id = ?")
@FilterDef(name = "deletedFeedbackFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedFeedbackFilter", condition = "is_deleted = false")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int feedbackId;

    private int courseRating;

    private int instructorRating;

    @Column(length = 1000)
    private String courseComment;
    @Column(length = 1000)
    private String instructorComment;

    private boolean anonymous;

    private LocalDateTime deletedAt;
    private String deletedBy;

    private String restoredBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @Column(nullable = false)
    private LocalDate submittedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}

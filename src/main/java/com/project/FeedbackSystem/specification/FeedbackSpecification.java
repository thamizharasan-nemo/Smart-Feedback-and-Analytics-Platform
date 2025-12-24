package com.example.FeedbackSystem.specification;

import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Feedback;
import com.example.FeedbackSystem.model.User;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class FeedbackSpecification {

    public static Specification<Feedback> hasCourseId(Integer courseId) {
        return ((root, query, criteriaBuilder) ->
                courseId == null ? null : criteriaBuilder.equal(root.get("course").get("courseId"), courseId));
    }

    public static Specification<Feedback> hasStudentId(Integer studentId) {
        return ((root, query, criteriaBuilder) ->
                studentId == null ? null : criteriaBuilder.equal(root.get("student").get("userId"), studentId));
    }

    public static Specification<Feedback> courseRatingGreaterThan(Integer courseRating) {
        return ((root, query, criteriaBuilder) ->
                courseRating == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("courseRating"), courseRating));
    }

    public static Specification<Feedback> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) ->
                keyword == null || keyword.isEmpty() ? null
                        : criteriaBuilder.like(criteriaBuilder.lower(root.get("courseComment")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Feedback> hasStudentName(String studentName) {
        return ((root, query, criteriaBuilder) ->
                studentName == null || studentName.isEmpty() ? null
                        : criteriaBuilder.like(root.join("student").get("username"), "%" + studentName.toLowerCase() + "%"));
    }

    public static Specification<Feedback> feedbackSubmittedBetween(LocalDate fromDate, LocalDate toDate) {
        return ((root, query, criteriaBuilder) -> {
            if (fromDate != null && toDate != null) {
                return criteriaBuilder.between(root.get("submittedAt"), fromDate, toDate);
            } else if (fromDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("submittedAt"), fromDate);
            } else if (toDate != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("submittedAt"), toDate);
            } else {
                return criteriaBuilder.conjunction();
            }
        }
        );
    }

    public static Specification<Feedback> anonymousFeedbacks(Boolean onlyAnonymous) {
        return (root, query, cb) -> {
            if (onlyAnonymous == null) return null;
            return cb.equal(root.get("anonymous"), onlyAnonymous);
        };
    }
}

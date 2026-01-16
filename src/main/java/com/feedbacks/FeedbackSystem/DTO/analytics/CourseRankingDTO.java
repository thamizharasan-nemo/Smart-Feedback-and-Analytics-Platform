package com.feedbacks.FeedbackSystem.DTO.analytics;

public record CourseRankingDTO(
        Integer courseId,
        String courseName,
        Double avgRating,
        Long feedbackCount
){}

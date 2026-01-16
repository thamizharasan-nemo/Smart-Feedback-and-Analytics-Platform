package com.feedbacks.FeedbackSystem.DTO.analytics;

public record InstructorRankingDTO(
        Integer instructorId,
        String instructorName,
        Double instructorRating
) {}

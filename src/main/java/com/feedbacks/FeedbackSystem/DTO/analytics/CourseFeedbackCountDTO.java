package com.feedbacks.FeedbackSystem.DTO.analytics;

public interface CourseFeedbackCountDTO {
    String getCourseName();
    int getCourseId();
    Long getFeedbackCount();
    Double getAvgRating();
}

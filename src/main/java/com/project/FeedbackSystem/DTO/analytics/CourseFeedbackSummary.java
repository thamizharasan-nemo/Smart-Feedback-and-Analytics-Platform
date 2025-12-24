package com.example.FeedbackSystem.DTO.analytics;


public interface CourseFeedbackSummary {
    int getCourseId();
    String getCourseName();
    String getInstructorName();
    Double getAverageRating();
    Long getFeedbackCount();
}

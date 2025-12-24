package com.example.FeedbackSystem.DTO.analytics;

public interface TopRatedInstructorsDTO {
    int getInstructorId();
    String getInstructorName();
    String getCourseName();
    Integer getTotalFeedbackCount();
    Double getAvgRating();
}

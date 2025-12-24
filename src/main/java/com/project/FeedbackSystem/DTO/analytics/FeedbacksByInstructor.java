package com.example.FeedbackSystem.DTO.analytics;

import java.util.List;

public interface FeedbacksByInstructor {
    Integer getInstructorId();
    String getInstructorName();
    String getCourseName();
    List<FeedbackProjection> getFeedbacksByCourse();
}

package com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorResponseDTO {
    int instructorId;
    String instructorName;
    List<CourseResponseDTO> assignedCourses;
    double avgRating;
    long feedbackCount;
    String deletedAt;
    String deletedBy;
    String restoredBy;

}

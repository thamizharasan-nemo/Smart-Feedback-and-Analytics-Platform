package com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponseDTO {
    private int feedbackId;
    private int courseRating;
    private String courseName;
    private boolean anonymous;
    private String studentName;
    private int instructorRating;
    private String courseComment;
    private String instructorName;
    private LocalDate submittedAt;
    private String instructorComment;
    private String deletedAt;
    private String deletedBy;
    private String restoredBy;
}

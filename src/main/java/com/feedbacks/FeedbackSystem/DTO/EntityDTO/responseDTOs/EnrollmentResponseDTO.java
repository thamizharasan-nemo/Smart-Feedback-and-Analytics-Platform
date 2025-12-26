package com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollmentResponseDTO {

    private int enrollmentId;
    private String studentName;
    private String studentRollNo;
    private String courseName;
    private String instructorName;
    private LocalDate enrolledDate;
}

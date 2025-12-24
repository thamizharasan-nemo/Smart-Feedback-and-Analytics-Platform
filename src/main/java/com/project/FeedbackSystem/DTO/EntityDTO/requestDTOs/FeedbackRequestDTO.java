package com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequestDTO {

    @NotNull(message = "Course rating is required")
    @Min(value = 1)
    @Max(value = 5)
    private int courseRating;

    private String courseComment;

    @NotNull(message = "Instructor rating is required")
    @Min(value = 1)
    @Max(value = 5)
    private int instructorRating;

    private String instructorComment;

    private boolean anonymous;

    @NotNull(message = "Student_Id is required")
    private int studentId;
    @NotNull(message = "Course_Id is required")
    private int courseId;
}

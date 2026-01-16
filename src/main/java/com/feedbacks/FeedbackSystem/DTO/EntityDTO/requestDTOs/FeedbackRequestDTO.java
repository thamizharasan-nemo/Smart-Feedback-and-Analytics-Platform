package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "Student_id is required")
    private Integer studentId;
    @NotNull(message = "Course_id is required")
    private Integer courseId;
    @NotNull(message = "instructor_id is required")
    private Integer instructorId;
}

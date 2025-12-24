package com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs;


import com.example.FeedbackSystem.model.Instructor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseRequestDTO {
    @NotBlank(message = "Course name is required")
    private String courseName;
    @NotBlank(message = "Course description is required")
    private String courseDescription;
    private Integer instructorId;
}

package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InstructorRequestDTO {
    @NotBlank(message = "Teacher name must exist")
    private String instructorName;
}

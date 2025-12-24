package com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs;

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
}

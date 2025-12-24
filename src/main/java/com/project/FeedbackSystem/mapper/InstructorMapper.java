package com.example.FeedbackSystem.mapper;

import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.example.FeedbackSystem.model.Instructor;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class InstructorMapper {

    public Instructor toEntity(@Valid InstructorRequestDTO requestDTO){
        Instructor instructor = new Instructor();
        instructor.setInstructorName(requestDTO.getInstructorName());
        return instructor;
    }

    public InstructorResponseDTO toResponse(Instructor instructor){
        InstructorResponseDTO responseDTO = new InstructorResponseDTO();
        responseDTO.setInstructorId(instructor.getInstructorId());
        responseDTO.setInstructorName(instructor.getInstructorName());
        responseDTO.setAssignedCourses(
                instructor.getCourses().stream()
                        .map(course -> new CourseResponseDTO(
                                course.getCourseId(),
                                course.getCourseName(),
                                course.getCourseDescription(),
                                course.getInstructor().getInstructorName())
                        ).toList()
        );
        return responseDTO;
    }
}

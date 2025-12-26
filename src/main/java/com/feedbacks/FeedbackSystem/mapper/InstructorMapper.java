package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.feedbacks.FeedbackSystem.model.Instructor;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

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

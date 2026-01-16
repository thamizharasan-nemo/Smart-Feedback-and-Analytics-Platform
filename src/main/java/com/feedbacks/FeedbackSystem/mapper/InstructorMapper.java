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
                                course.getInstructor().getInstructorName(),
                                course.getDeletedAt() != null ? course.getDeletedAt().toString() : "Not yet deleted",
                                course.getDeletedBy(),
                                course.getRestoredBy())
                        ).toList()
        );
        responseDTO.setAvgRating(instructor.getAvgRating());
        responseDTO.setFeedbackCount(instructor.getFeedbackCount());
        responseDTO.setDeletedAt(instructor.getDeletedAt() != null ? instructor.getDeletedAt().toString() : "not yet deleted");
        responseDTO.setDeletedBy(instructor.getDeletedBy() != null ? instructor.getDeletedBy() : "No one");
        responseDTO.setRestoredBy(instructor.getRestoredBy() != null ? instructor.getRestoredBy() : "No one");
        return responseDTO;
    }
}

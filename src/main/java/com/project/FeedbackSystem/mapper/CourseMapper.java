package com.example.FeedbackSystem.mapper;

import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.CourseRequestDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Instructor;
import com.example.FeedbackSystem.service.InstructorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    private final InstructorService instructorService;

    public CourseMapper(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    public Course toEntity(@Valid CourseRequestDTO requestDTO){
        Course course = new Course();
        course.setCourseName(requestDTO.getCourseName());
        course.setCourseDescription(requestDTO.getCourseDescription());

        Instructor instructor = instructorService.getInstructorById(requestDTO.getInstructorId());
        course.setInstructor(instructor);

        return course;
    }

    public CourseResponseDTO toResponse(Course course){
        Instructor instructor = null;
        String instructorName = null;

        if(course.getInstructor() != null){
            instructor = instructorService.getInstructorById(course.getInstructor().getInstructorId());
        }
        if(instructor != null){
            instructorName = instructor.getInstructorName();
        }

        return new CourseResponseDTO(
                course.getCourseId(),
                course.getCourseName(),
                course.getCourseDescription(),
                instructorName
        );
    }

    public Course forGettingExists(Course course, @Valid CourseRequestDTO requestDTO){
        course.setCourseName(requestDTO.getCourseName());
        course.setCourseDescription(requestDTO.getCourseDescription());
        Instructor instructor = instructorService.getInstructorById(requestDTO.getInstructorId());
        course.setInstructor(instructor);
        return course;
    }
}

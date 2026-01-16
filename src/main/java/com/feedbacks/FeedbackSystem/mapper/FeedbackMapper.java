package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Feedback;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.service.serviceImple.CourseServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.InstructorServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FeedbackMapper {

    private final UserServiceImpl userService;
    private final CourseServiceImpl courseService;
    private final InstructorServiceImpl instructorService;

    public FeedbackMapper(UserServiceImpl userService, CourseServiceImpl courseService, InstructorServiceImpl instructorService) {
        this.userService = userService;
        this.courseService = courseService;
        this.instructorService = instructorService;
    }

    public Feedback toEntity(FeedbackRequestDTO feedbackRequestDTO,
                             Feedback feedback,
                             Course course,
                             User student,
                             Instructor instructor){
        feedback.setCourseRating(feedbackRequestDTO.getCourseRating());
        feedback.setInstructorRating(feedbackRequestDTO.getInstructorRating());
        feedback.setCourseComment(feedbackRequestDTO.getCourseComment());
        feedback.setInstructorComment(feedbackRequestDTO.getInstructorComment());
        feedback.setAnonymous(feedbackRequestDTO.isAnonymous());
        feedback.setStudent(student);
        feedback.setCourse(course);
        feedback.setInstructor(instructor);
        feedback.setSubmittedAt(LocalDate.now());

        return feedback;
    }

    public FeedbackResponseDTO toResponse(@Valid Feedback feedback){
        return new FeedbackResponseDTO(
                feedback.getFeedbackId(),
                feedback.getCourseRating(),
                feedback.getCourse().getCourseName(),
                feedback.isAnonymous(),
                feedback.isAnonymous() ? "Anonymous" : feedback.getStudent().getUsername(),
                feedback.getInstructorRating(),
                feedback.getCourseComment(),
                feedback.getCourse().getInstructor().getInstructorName(),
                feedback.getSubmittedAt(),
                feedback.getInstructorComment(),
                feedback.getDeletedAt() != null ? feedback.getDeletedAt().toString() : "Not yet deleted",
                feedback.getDeletedBy(),
                feedback.getRestoredBy()
        );
    }
}

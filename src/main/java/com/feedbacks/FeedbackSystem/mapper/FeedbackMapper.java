package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Feedback;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.service.CourseService;
import com.feedbacks.FeedbackSystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FeedbackMapper {

    private final UserService userService;
    private final CourseService courseService;

    public FeedbackMapper(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
    }

    public Feedback toEntity(FeedbackRequestDTO feedbackRequestDTO, Feedback feedback){
        //converting FeedbackDTO into Feedback
        int studentId = feedbackRequestDTO.getStudentId();
        User student = userService.getUserById(studentId);

        int courseId = feedbackRequestDTO.getCourseId();
        Course course = courseService.getCourseById(courseId);
        //mapping fields
        feedback.setCourseRating(feedbackRequestDTO.getCourseRating());
        feedback.setInstructorRating(feedbackRequestDTO.getInstructorRating());
        feedback.setCourseComment(feedbackRequestDTO.getCourseComment());
        feedback.setInstructorComment(feedbackRequestDTO.getInstructorComment());
        feedback.setAnonymous(feedbackRequestDTO.isAnonymous());
        feedback.setStudent(student);
        feedback.setCourse(course);
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
                feedback.getInstructorComment()
        );
    }
}

package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.EnrollmentRepository;
import com.feedbacks.FeedbackSystem.service.CourseService;
import com.feedbacks.FeedbackSystem.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepo;

    public EnrollmentMapper(UserService userService, CourseService courseService, EnrollmentRepository enrollmentRepo) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentRepo = enrollmentRepo;
    }

    public Enrollment toEntity(EnrollmentRequestDTO requestDTO, Enrollment enrollment){
        User student = userService.getUserById(requestDTO.getStudentId());
        Course course = courseService.getCourseById(requestDTO.getCourseId());

        if(!student.getRole().toString().equals("STUDENT")){
            throw new NotAllowedException("Only students can enroll to courses.");
        }

        int enrollmentCount = enrollmentRepo.countStudentTotalEnrollments(requestDTO.getStudentId());
        if(enrollmentCount >= 10){
            throw new NotAllowedException("A user can only enroll up to 10 courses.");
        }

        if(enrollmentRepo.existsByCourseIdAndStudentId(requestDTO.getCourseId(), requestDTO.getStudentId())){
            throw new BadRequestException("Student with rollNo "+student.getIdentityNo()+" already enrolled in this course");
        }

        enrollment.setStudent(student);
        enrollment.setCourse(course);
        return enrollment;
    }

    public EnrollmentResponseDTO toResponse(Enrollment enrollment){
        EnrollmentResponseDTO responseDTO = new EnrollmentResponseDTO();
        responseDTO.setEnrollmentId(enrollment.getEnrollId());
        responseDTO.setStudentName(enrollment.getStudent().getUsername());
        responseDTO.setStudentRollNo(enrollment.getStudent().getIdentityNo());
        responseDTO.setCourseName(enrollment.getCourse().getCourseName());
        responseDTO.setInstructorName(enrollment.getCourse().getInstructor().getInstructorName());
        responseDTO.setEnrolledDate(enrollment.getEnrollmentDate());
        return responseDTO;
    }
}

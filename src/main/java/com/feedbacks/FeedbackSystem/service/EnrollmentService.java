package com.feedbacks.FeedbackSystem.service;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.EnrollmentMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentService(EnrollmentRepository enrollmentRepo, UserService userService, CourseService courseService, EnrollmentMapper enrollmentMapper) {
        this.enrollmentRepo = enrollmentRepo;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentMapper = enrollmentMapper;
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepo.findAll();
    }

    public Enrollment getEnrollmentById(int enrollmentId) {
        return enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No enrollment found."));
    }

    public List<EnrollmentResponseDTO> getAllEnrollmentsByStudentId(int studentId) {
        User student = userService.getUserById(studentId);
        List<Enrollment> enrollments = enrollmentRepo.findByStudent(student);

        return enrollments.stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponseDTO> getAllEnrollmentsByRollNo(String rollNo) {
        List<Enrollment> enrollments = enrollmentRepo.findByStudent_IdentityNo(rollNo);
        if(enrollments.isEmpty()){
            throw new ResourceNotFoundException("No enrollments found for roll number "+rollNo);
        }
        return enrollments.stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }



    public EnrollmentResponseDTO enrollToCourse(EnrollmentRequestDTO requestDTO){
        Enrollment enrollment = new Enrollment();
        enrollment = enrollmentMapper.toEntity(requestDTO, enrollment);
        if (enrollment == null) {
            throw new ResourceNotFoundException("Enrollment failed");
        }
        enrollmentRepo.save(enrollment);
        return enrollmentMapper.toResponse(enrollment);
    }


    public EnrollmentResponseDTO findEnrollmentByStudentIdAndCourseId(EnrollmentRequestDTO requestDTO){
        Enrollment enrollment = enrollmentRepo
                .findEnrollmentByStudentUserIdAndCourseCourseId(requestDTO.getStudentId(), requestDTO.getCourseId());
        if(enrollment == null){
            throw new ResourceNotFoundException("No enrollment found for student to this course.");
        }
        return enrollmentMapper.toResponse(enrollment);
    }

    public void unrollToCourse(EnrollmentRequestDTO requestDTO){
        User student = userService.getUserById(requestDTO.getStudentId());
        Course course = courseService.getCourseById(requestDTO.getCourseId());

        if(!enrollmentRepo.existsByCourseIdAndStudentId(requestDTO.getCourseId(), requestDTO.getStudentId())){
            throw new NotAllowedException("The student with roll number "+student.getIdentityNo()+" hasn't enrolled to this course "+course.getCourseName());
        }

        Enrollment enrollment = enrollmentRepo
                .findEnrollmentByStudentUserIdAndCourseCourseId(requestDTO.getStudentId(), requestDTO.getCourseId());

        enrollmentRepo.delete(enrollment);
    }

    public int getCourseEnrollmentCount(int courseId){
        return enrollmentRepo.getCourseEnrollmentCount(courseId);
    }

    public int getStudentEnrollmentCount(int studentId) {
        return enrollmentRepo.getStudentEnrollmentCount(studentId);
    }
}

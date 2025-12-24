package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.example.FeedbackSystem.Exception.BadRequestException;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Enrollment;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    EnrollmentRepository enrollmentRepo;

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepo.findAll();
    }

    public Enrollment getEnrollmentById(int enrollmentId) {
        return enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("No enrollment found."));
    }

    public List<EnrollmentResponseDTO> getAllEnrollmentsByStudentId(int studentId) {
        User student = userService.getUserById(studentId);
        List<Enrollment> enrollments = enrollmentRepo.findByStudent(student);

        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponseDTO> getAllEnrollmentsByRollNo(String rollNo) {
        List<Enrollment> enrollments = enrollmentRepo.findByStudent_IdentityNo(rollNo);
        if(enrollments.isEmpty()){
            throw new ResourceNotFoundException("No enrollments found for roll number "+rollNo);
        }
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EnrollmentResponseDTO convertToDTO(Enrollment enrollment){
        EnrollmentResponseDTO enrollmentResponse = new EnrollmentResponseDTO();
        enrollmentResponse.setEnrollmentId(enrollment.getEnrollId());
        enrollmentResponse.setStudentName(enrollment.getStudent().getUsername());
        enrollmentResponse.setStudentRollNo(enrollment.getStudent().getIdentityNo());
        enrollmentResponse.setCourseName(enrollment.getCourse().getCourseName());
        enrollmentResponse.setInstructorName(enrollment.getCourse().getInstructor().getInstructorName());
        enrollmentResponse.setEnrolledDate(enrollment.getEnrollmentDate());
        return enrollmentResponse;
    }

    public EnrollmentResponseDTO enrollToCourse(int studentId, int courseId){
        User student = userService.getUserById(studentId);
        Course course = courseService.getCourseById(courseId);

        if(!student.getRole().toString().equals("STUDENT")){
            throw new RuntimeException("Only students can enroll to courses.");
        }

        int enrollmentCount = enrollmentRepo.countStudentTotalEnrollments(studentId);
        if(enrollmentCount >= 10){
            throw new BadRequestException("A user can only enroll up to 10 courses.");
        }

        if(enrollmentRepo.existsByCourseIdAndStudentId(courseId, studentId)){
            throw new BadRequestException("Student with rollNo "+student.getIdentityNo()+" already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(enrollment.getEnrollmentDate());
        enrollmentRepo.save(enrollment);

        return convertToDTO(enrollment);
    }

    public EnrollmentResponseDTO getEnrollmentResponse(Enrollment enrollment){
        try {
            return convertToDTO(enrollment);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage()+" Error while converting to enrollment response");
        }
    }

    public EnrollmentResponseDTO findEnrollmentByStudentIdAndCourseId(int studentId, int courseId){
        Enrollment enrollment = enrollmentRepo.findEnrollmentByStudentUserIdAndCourseCourseId(studentId, courseId);
        if(enrollment == null){
            throw new ResourceNotFoundException("No enrollment found for student to this course.");
        }
        return convertToDTO(enrollment);
    }

    public void unrollToCourse(int studentId, int courseId){
        User student = userService.getUserById(studentId);
        Course course = courseService.getCourseById(courseId);

        if(!enrollmentRepo.existsByCourseIdAndStudentId(courseId, studentId)){
            throw new RuntimeException("The student with roll number "+student.getIdentityNo()+" hasn't enrolled in this course.");
        }

        Enrollment enrollment = enrollmentRepo.findEnrollmentByStudentUserIdAndCourseCourseId(studentId, courseId);

        enrollmentRepo.delete(enrollment);
    }

    public int getCourseEnrollmentCount(int courseId){
        return enrollmentRepo.getCourseEnrollmentCount(courseId);
    }

    public int getStudentEnrollmentCount(int studentId) {
        return enrollmentRepo.getStudentEnrollmentCount(studentId);
    }
}

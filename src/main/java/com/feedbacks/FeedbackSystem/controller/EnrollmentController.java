package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Enrollment>> getAllEnrollments(){
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{enrollmentId}")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable int enrollmentId){
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(enrollmentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getEnrollmentByStudentId(@PathVariable int studentId){
        return ResponseEntity.ok(enrollmentService.getAllEnrollmentsByStudentId(studentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/student/rollNo/{rollNo}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getEnrollmentByRollNo(@PathVariable String rollNo){
        return ResponseEntity.ok(enrollmentService.getAllEnrollmentsByRollNo(rollNo));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollToCourse(@RequestBody EnrollmentRequestDTO requestDTO){
        return ResponseEntity.ok(enrollmentService.enrollToCourse(requestDTO));
    }

    @DeleteMapping("/unroll")
    public ResponseEntity<?> unrollToCourse(@RequestBody EnrollmentRequestDTO requestDTO){
        enrollmentService.unrollToCourse(requestDTO);
        return ResponseEntity.ok("Student unrolled successfully");
    }

    @GetMapping("/enrollments/student/course")
    public ResponseEntity<EnrollmentResponseDTO> findEnrollmentByIds(@RequestBody EnrollmentRequestDTO requestDTO){
        return ResponseEntity.ok(enrollmentService.findEnrollmentByStudentIdAndCourseId(requestDTO));
    }

    @GetMapping("/count/course-enrollment/{courseId}")
    public ResponseEntity<?> getCourseEnrollmentCount(@PathVariable int courseId){
        return ResponseEntity.ok("Total enrollments: "+enrollmentService.getCourseEnrollmentCount(courseId));
    }

    @GetMapping("/count/student-enrollment/{studentId}")
    public ResponseEntity<?> getStudentEnrollmentCount(@PathVariable int studentId){
        return ResponseEntity.ok("Total enrollments: "+enrollmentService.getStudentEnrollmentCount(studentId));
    }

}
package com.example.FeedbackSystem.controller;

import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.example.FeedbackSystem.model.Enrollment;
import com.example.FeedbackSystem.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    @Autowired
    EnrollmentService enrollmentService;

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
    @PostMapping("/enroll/student/{studentId}/course/{courseId}")
    public ResponseEntity<?> enrollToCourse(@PathVariable int studentId, @PathVariable int courseId){
        return ResponseEntity.ok(enrollmentService.enrollToCourse(studentId, courseId));
    }


    @GetMapping("/{studentId}/enrollments/{courseId}")
    public ResponseEntity<EnrollmentResponseDTO> findEnrollmentByIds(@PathVariable int studentId,@PathVariable int courseId){
        return ResponseEntity.ok(enrollmentService.findEnrollmentByStudentIdAndCourseId(studentId, courseId));
    }

    @DeleteMapping("/{studentId}/unroll-course/{courseId}")
    public ResponseEntity<?> unrollToCourse(@PathVariable int studentId, @PathVariable int courseId){
        enrollmentService.unrollToCourse(studentId, courseId);
        return ResponseEntity.ok("Student unrolled successfully");
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
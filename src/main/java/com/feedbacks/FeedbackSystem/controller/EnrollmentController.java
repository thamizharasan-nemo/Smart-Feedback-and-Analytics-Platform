package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.service.serviceImple.EnrollmentServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentServiceImpl enrollmentService;

    public EnrollmentController(EnrollmentServiceImpl enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Enrollment>> getAllEnrollments(){
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{enrollmentId}")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable Integer enrollmentId){
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(enrollmentId));
    }

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> getEnrollmentResponseById(@PathVariable Integer enrollmentId){
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Enrollment data retrieved",
                        enrollmentService.getEnrollmentResponseById(enrollmentId)
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentByStudentId(@PathVariable Integer studentId){
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Whole enrollment list",
                        enrollmentService.getAllEnrollmentsByStudentId(studentId)
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/student")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentByRollNo(@RequestParam String rollNo){
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Whole enrollment list",
                        enrollmentService.getAllEnrollmentsByRollNo(rollNo)
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @PostMapping
    public ResponseEntity<EnrollmentResponseDTO> enrollToCourse(@RequestBody EnrollmentRequestDTO requestDTO){
        return ResponseEntity.ok(enrollmentService.enrollToCourse(requestDTO));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> unrollToCourse(@RequestBody EnrollmentRequestDTO requestDTO){
        enrollmentService.unrollToCourse(requestDTO);
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "unrolled successfully",
                        "Student unrolled successfully"
                )
        );
    }

    @GetMapping("/studentId/courseId")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> findEnrollmentByIds(@RequestBody EnrollmentRequestDTO requestDTO){
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Enrollment for student with course",
                enrollmentService.findEnrollmentByStudentIdAndCourseId(requestDTO))
        );
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<String> getCourseEnrollmentCount(@PathVariable Integer courseId){
        return ResponseEntity.ok("Total enrollments: "+enrollmentService.getCourseEnrollmentCount(courseId));
    }

    @GetMapping("/student/{studentId}/count")
    public ResponseEntity<String> getStudentEnrollmentCount(@PathVariable Integer studentId){
        return ResponseEntity.ok("Total enrollments: "+enrollmentService.getStudentEnrollmentCount(studentId));
    }

}
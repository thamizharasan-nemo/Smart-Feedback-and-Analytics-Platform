package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;

import java.util.List;

public interface EnrollmentService {
    EnrollmentResponseDTO getEnrollmentResponseById(int enrollmentId);

    List<EnrollmentResponseDTO> getAllEnrollmentsByStudentId(int studentId);

    List<EnrollmentResponseDTO> getAllEnrollmentsByRollNo(String rollNo);

    EnrollmentResponseDTO enrollToCourse(EnrollmentRequestDTO requestDTO);

    EnrollmentResponseDTO findEnrollmentByStudentIdAndCourseId(EnrollmentRequestDTO requestDTO);

    void unrollToCourse(EnrollmentRequestDTO requestDTO);

    int getCourseEnrollmentCount(int courseId);

    int getStudentEnrollmentCount(int studentId);
}

package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.feedbacks.FeedbackSystem.model.Instructor;
import jakarta.transaction.Transactional;

import java.util.List;

public interface InstructorService {
    List<Instructor> getAllInstructor();

    // Helper to all other methods
    Instructor getInstructorById(Integer instructorId);

    InstructorResponseDTO getInstructorResponseById(int instructorId);

    @Transactional
    void updateInstructorStateOnFeedbackAdd(Instructor instructor, int rating, boolean isEditing);

    @Transactional
    void updateInstructorStateOnFeedbackRemove(Instructor instructor, int rating);

    InstructorResponseDTO addInstructor(InstructorRequestDTO requestDTO);

    InstructorResponseDTO updateInstructorById(int instructorId, InstructorRequestDTO requestDTO);

    void deleteInstructorById(int instructorId);

    InstructorResponseDTO assignCourseToInstructor(int instructorId, int courseId);

    void unassignCourseFromInstructor(int instructorId, int courseId);

    List<InstructorResponseDTO> findAllSoftDeletedInstructors();

    InstructorResponseDTO restoreInstructor(int instructorId);

    @Transactional
    void deleteInstructorPermanently(int instructorId);

    List<InstructorResponseDTO> getUnassignedInstructors();

    List<String> viewAssignedCourseForInstructor(int instructorId);

    List<FeedbacksByInstructor> getAllFeedbacksByInstructor();

    List<TopRatedInstructorsDTO> getAllTopRatedInstructors();

    List<InstructorResponseDTO> searchInstructor(Integer instructorId,
                                                 String instructorName,
                                                 String courseName
    );

    List<InstructorRankingDTO> getTopRatedInstructor(int page, int size);
}

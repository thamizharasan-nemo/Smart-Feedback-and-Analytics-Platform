package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

public interface FeedbackService {
    FeedbackResponseDTO getFeedbackResponseById(Integer feedbackId);

    List<FeedbackResponseDTO> getFeedbacks();

    FeedbackResponseDTO submitFeedback(@Valid FeedbackRequestDTO feedbackRequestDTO);

    FeedbackResponseDTO editFeedback(Integer feedbackId,
                                     @Valid FeedbackRequestDTO feedbackRequestDTO);

    @Transactional
    void deleteFeedbackById(int userId, int feedbackId);

    List<FeedbackResponseDTO> findAllSoftDeletedFeedbacks();

    FeedbackResponseDTO restoreFeedback(int userId, int feedbackId);

    @Transactional
    void deleteFeedbackPermanently(int userId, int feedbackId);

    //Sorting Method
    Sort sortingFunction(String sort);

    Page<FeedbackResponseDTO> getSortedFeedbackByCourseId(int courseId,
                                                          int page,
                                                          int size,
                                                          String sort);

    List<FeedbackResponseDTO> getFilteredFeedback(int courseId, int minRating, int maxRating,
                                                  LocalDate fromDate, LocalDate toDate, boolean anonymous);

    Page<FeedbackResponseDTO> getFeedbackByUserId(int userId, int pageNo,
                                                  int size, String sortString);

    Double getAverageRatingForCourse(int courseId);

    Double getAverageRatingForInstructor(int instructorId);

    List<CourseFeedbackSummary> getCourseSummary();

    List<FeedbackResponseDTO> getFeedbacksByStudentAndCourse(int studentId, int courseId);

    List<FeedbackResponseDTO> getRecentFeedbacksByCourseId(int courseId);

    List<FeedbackResponseDTO> searchFeedback(Integer courseId, Integer studentId,
                                             Integer minRating, String keyword,
                                             String studentName, Boolean anonymous,
                                             LocalDate fromDate, LocalDate toDate
    );

    List<FeedbackTrendDTO> getFeedbackTrends(String groupBy);

    List<RatingDistributionDTO> getFeedbackRatings();
}

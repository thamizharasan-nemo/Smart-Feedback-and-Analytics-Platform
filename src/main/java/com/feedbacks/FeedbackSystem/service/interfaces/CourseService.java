package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.CourseRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackCountDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.PopularCourseDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface CourseService {
    CourseResponseDTO getCourseResponseById(int courseId);

    CourseResponseDTO addCourse(CourseRequestDTO requestDTO);

    @Transactional
    void updateCourseStateOnFeedbackAdd(Course course, int rating, boolean isEditing);

    @Transactional
    void updateCourseStateOnFeedbackRemove(Course course, int rating);

    CourseResponseDTO updateCourseById(int courseId, CourseRequestDTO requestDTO);

    // @SqlDelete Soft Delete the record instead of physically deleting it
    void deleteCourseById(int courseId);

    List<CourseResponseDTO> findAllSoftDeletedCourses();

    // To restore the deleted records
    CourseResponseDTO restoreCourse(int courseId);

    @Transactional
    void deleteCoursePermanently(int courseId);

    CourseResponseDTO assignInstructorToCourse(int courseId, int instructorId);

    CourseResponseDTO unassignInstructorToCourse(int courseId);

    Page<CourseResponseDTO> getCoursesScalable(Integer courseId,
                                               String courseName,
                                               Integer instructorId,
                                               String instructorName,
                                               Boolean popular,
                                               Integer minEnrollments,
                                               Double minAvgRating,
                                               String sortBy,
                                               String sortDirection,
                                               int page, int size);

    List<CourseResponseDTO> searchCourseByName(String courseName);

    List<CourseResponseDTO> getCoursesForInstructor(int instructorId);

    List<CourseFeedbackCountDTO> getFeedbackCountAndAvg();

    List<CourseResponseDTO> getSortedCourses(String sortBy);

    List<PopularCourseDTO> getPopularCourses(int pageNumber, int pageSize);

    // Slice<T> is useful for infinite scrolling or “Load More” UI. & Doesn't count data
    Slice<PopularCourseDTO> getUnPopularCourses(int pageNumber, int pageSize);

    List<CourseResponseDTO> searchCourse(Integer courseId,
                                         Integer instructorId,
                                         String courseName,
                                         String instructorName
    );

    List<CourseResponseDTO> hasEnrollmentsGreaterThan(int minEnrollments);

    List<CourseResponseDTO> findCoursesWithoutFeedback();

    List<CourseResponseDTO> findCoursesNotAssigned();

    List<CourseResponseDTO> findCoursesLessThanAvgRating(Double avgRating);

    List<CourseRankingDTO> getCourseRanking(int page, int size);
}

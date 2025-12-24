package com.example.FeedbackSystem.repository;

import com.example.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.example.FeedbackSystem.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer>,
        JpaSpecificationExecutor<Feedback> {

    @Query("SELECT f from Feedback f " +
            "LEFT JOIN Course c " +
            "ON c.courseId = f.course.courseId " +
            "WHERE c.courseId = :courseId ")
    Page<Feedback> findByCourse_CourseId(int courseId, Pageable pageable);

    @Query(" SELECT f FROM Feedback f WHERE " +
            "(:courseId IS NULL OR f.course.courseId = :courseId) AND " +
            "(:minRating IS NULL OR f.courseRating >= :minRating) AND " +
            "(:maxRating IS NULL OR f.courseRating <= :maxRating) AND " +
            "(:fromDate IS NULL OR f.submittedAt >= :fromDate) AND " +
            "(:toDate IS NULL OR f.submittedAt <= :toDate) AND " +
            "(:anonymous IS NULL OR f.anonymous = :anonymous)")
    List<Feedback> filterFeedback(
            @Param("courseId") Integer courseId,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("anonymous") Boolean anonymous
            );


    Page<Feedback> findByStudentUserId(Integer userId, Pageable pageable);

    @Query("SELECT AVG(f.courseRating) FROM Feedback f WHERE f.course.courseId = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Integer courseId);

    @Query("SELECT AVG(f.instructorRating) FROM Feedback f WHERE f.course.instructor.instructorId = :instructorId")
    Double findAverageRatingByInstructorId(@Param("instructorId") Integer instructorId);

    @Query("SELECT f.course.courseId AS courseId, " +
            "f.course.courseName AS courseName, " +
            "f.course.instructor.instructorName AS instructorName, " +
            "(AVG(f.courseRating) + AVG(f.instructorRating)) / 2 AS averageRating, " +
            "COUNT(f.feedbackId) AS feedbackCount " +
            "FROM Feedback f " +
            "GROUP BY f.course.courseId, f.course.courseName")
    List<CourseFeedbackSummary> findCourseSummaries();

    List<Feedback> findByStudent_UserIdAndCourse_CourseId(Integer userId, Integer courseId);


    @Query("SELECT f FROM Feedback f " +
            "WHERE f.submittedAt >= :startDate AND f.course.courseId = :courseId " +
            "ORDER BY f.submittedAt DESC")
    List<Feedback> getRecentFeedbacksByCourseId(@Param("courseId") Integer courseId,
                                                @Param("startDate") LocalDate startDate);


    //Rate limiting (per-user)
    @Query("SELECT COUNT(f) FROM Feedback f " +
            "WHERE f.student.userId = :userId " +
            "AND DATE(f.submittedAt) = CURRENT_DATE")
    Integer countTodayFeedbacks(@Param("userId") Integer userId);
}

package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import com.feedbacks.FeedbackSystem.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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
    Page<Feedback> findByCourse_CourseId(Integer courseId, Pageable pageable);

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

    @Query(value = "SELECT * FROM Feedback f WHERE f.is_deleted = true", nativeQuery = true)
    List<Feedback> findAllDeletedFeedback();

    @Modifying
    @Query(value = "DELETE FROM Feedback f WHERE f.feedback_id = :feedbackId", nativeQuery = true)
    void deletePermanently(@Param("feedbackId") Integer feedbackId);

    @Modifying
    @Query("""
                UPDATE Feedback f
                SET f.isDeleted = true,
                    f.deletedAt = CURRENT_TIMESTAMP,
                    f.deletedBy = :name
                WHERE f.course.courseId = :courseId
            """)
    void softDeleteByCourse(Long courseId, String name);

    @Modifying
    @Query("""
                UPDATE Feedback f
                SET f.isDeleted = false,
                    f.deletedAt = null,
                    f.deletedBy = null
                WHERE f.course.courseId = :courseId
            """)
    void restoreByCourse(Long courseId);


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

    @Query("""
            SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO(
                     CONCAT('',FUNCTION('DATE_FORMAT', f.submittedAt, '%Y-%m')),
                     COUNT(f)
                     )
                     FROM Feedback f
                     GROUP BY FUNCTION('DATE_FORMAT', f.submittedAt, '%Y-%m')
                     ORDER BY FUNCTION('DATE_FORMAT', f.submittedAt, '%Y-%m')
            """)
    List<FeedbackTrendDTO> getMonthlyTrends();

    @Query("""
            SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO(
                     CONCAT('', FUNCTION('DATE_FORMAT', f.submittedAt, '%m-%d')),
                     COUNT(f)
                     )
                     FROM Feedback f
                     GROUP BY FUNCTION('DATE_FORMAT', f.submittedAt, '%m-%d')
                     ORDER BY FUNCTION('DATE_FORMAT', f.submittedAt, '%m-%d')
            """)
    List<FeedbackTrendDTO> getDailyTrends();

    @Query("""
            SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO(
                     CONCAT('', FUNCTION('DATE_FORMAT', f.submittedAt, '%Y')),
                     COUNT(f)
                     )
                     FROM Feedback f
                     GROUP BY FUNCTION('DATE_FORMAT', f.submittedAt, '%Y')
                     ORDER BY FUNCTION('DATE_FORMAT', f.submittedAt, '%Y')
            """)
    List<FeedbackTrendDTO> getYearlyTrends();

    @Query("""
            SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO(
            f.courseRating,
            COUNT(f)
            )
            FROM Feedback f
            GROUP BY f.courseRating
            ORDER BY f.courseRating
            """)
    List<RatingDistributionDTO> getRatingDistribution();


    // CREATE intermediate class between feedback and instructor as well as course
    @Query("""
            SELECT AVG(f.instructorRating) 
            FROM Feedback f
            WHERE f.submittedAt >= :date
            """)
    Double avgInstructRatingLast7Days(@Param("date") LocalDate date);

    @Query("""
            SELECT AVG(f.courseRating) 
            FROM Feedback f
            WHERE f.submittedAt >= :date
            """)
    Double avgCourseRatingLast7Days(@Param("date") LocalDate date);

    @Query("""
            SELECT f.instructorRating, COUNT(f) 
            FROM Feedback f
            WHERE f.instructor.instructorId = :instructorId
            GROUP BY f.instructorRating 
            """)
    List<Object[]> ratingDistribution(@Param("instructorId") Integer instructorId);
}

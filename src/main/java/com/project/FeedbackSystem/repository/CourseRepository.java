package com.example.FeedbackSystem.repository;

import com.example.FeedbackSystem.DTO.analytics.CourseFeedbackCountDTO;
import com.example.FeedbackSystem.DTO.analytics.PopularCourseDTO;
import com.example.FeedbackSystem.model.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer>,
        JpaSpecificationExecutor<Course> {
    //search course by name
    @Query("SELECT c FROM Course c " +
            "WHERE LOWER(c.courseName) LIKE LOWER(CONCAT ('%', :courseName, '%'))")
    List<Course> findByCourseNameContainingIgnoreCase(@Param("courseName") String courseName);

    @Query("SELECT c.courseId as courseId, " +
            "c.courseName as courseName, " +
            "COUNT(f) as feedbackCount, " +
            "AVG(f.courseRating) as avgRating " +
            "FROM Course c " +
            "LEFT JOIN c.feedbacks f " +
            "GROUP BY c.courseId, c.courseName" )
    List<CourseFeedbackCountDTO> countFeedbacksAndAvgRatePerCourse();

    // USE NATIVE QUERIES TO IGNORE THE FILTER CLAUSE IN COURSE ENTITY
    @Query(value = "SELECT * FROM course c " +
            "WHERE c.course_id = :courseId AND c.is_deleted = true", nativeQuery = true)
    Optional<Course> restoreCourseById(@Param("courseId")int courseId);

    @Query(value = "SELECT * FROM course c WHERE c.is_deleted = true", nativeQuery = true)
    List<Course> findAllDeletedCourses();

    @Modifying
    @Query(value = "DELETE FROM course c WHERE c.course_id = :courseId", nativeQuery = true)
    void deletePermanently(@Param("courseId")int courseId);

    @Query("SELECT c.courseId AS courseId, " +
            "c.courseName AS courseName, " +
            "COUNT(f) AS feedbackCount, " +
            "i.instructorName AS instructorName " +
            "FROM Course c " +
            "LEFT JOIN c.instructor i " +
            "LEFT JOIN c.feedbacks f " +
            "GROUP BY c.courseId, c.courseName " +
            "ORDER BY feedbackCount DESC")
    List<PopularCourseDTO> findPopularCourses(Pageable pageable);

    // Slice class to do "load more" in frontend
    @Query("SELECT c.courseId AS courseId, " +
            "c.courseName AS courseName, " +
            "COUNT(f) AS feedbackCount, " +
            "i.instructorName AS instructorName " +
            "FROM Course c " +
            "LEFT JOIN c.instructor i " +
            "LEFT JOIN c.feedbacks f " +
            "GROUP BY c.courseId, c.courseName " +
            "ORDER BY feedbackCount ASC")
    Slice<PopularCourseDTO> findUnPopularCourses(Pageable pageable);

    @Query("SELECT c FROM Course c " +
            "LEFT JOIN c.enrollments e " +
            "GROUP BY c.courseId " +
            "HAVING COUNT(e) >= :minEnrollment")
    List<Course> hasEnrollmentGreaterThan(int minEnrollment);
}

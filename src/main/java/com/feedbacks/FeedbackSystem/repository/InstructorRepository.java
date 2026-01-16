package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Instructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InstructorRepository extends JpaRepository<Instructor, Integer>,
        JpaSpecificationExecutor<Instructor> {

    @Query(value = "SELECT i.instructorId AS instructorId, " +
            "i.instructorName AS instructorName, " +
            "c.courseName AS courseName, " +
            "f AS feedbacksByCourse " +
            "FROM Instructor i " +
            "LEFT JOIN i.courses c " +
            "LEFT JOIN c.feedbacks f " )
    List<FeedbacksByInstructor> getAllFeedbacksByInstructor();

    @Query("SELECT i FROM Instructor i LEFT JOIN i.courses c WHERE c IS NULL")
    List<Instructor> findUnassignedInstructors();

    @Query(value = "SELECT * FROM Instructor i WHERE i.is_deleted = true", nativeQuery = true)
    List<Instructor> findAllDeletedInstructor();

    @Modifying
    @Query(value = "DELETE FROM Instructor i WHERE i.instructor_id = :instructorId", nativeQuery = true)
    void deletePermanently(@Param("instructorId")Integer instructorId);


    // JOIN FETCH - SLOW
    @Query("SELECT i.instructorId AS instructorId, " +
            "i.instructorName AS instructorName, " +
            "c.courseName AS courseName, " +
            "COUNT(f) AS totalFeedbackCount, " +
            "AVG(f.instructorRating) AS avgRating " +
            "FROM Instructor i " +
            "LEFT JOIN i.courses c " +
            "LEFT JOIN c.feedbacks f " +
            "GROUP BY i.instructorId, i.instructorName, c.courseName " +
            "ORDER BY avgRating DESC ")
    List<TopRatedInstructorsDTO> findTopRatedInstructor();

    // MATERIALIZED VIEW - FAST
    @Query("""
    SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO(
            i.instructorId,
            i.instructorName,
            i.avgRating
    )
            FROM Instructor i
            GROUP BY i.instructorId
            ORDER BY i.avgRating DESC
    """)
    List<InstructorRankingDTO> getTopRatedInstructor(Pageable pageable);



}

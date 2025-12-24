package com.example.FeedbackSystem.repository;

import com.example.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.example.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.example.FeedbackSystem.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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
}

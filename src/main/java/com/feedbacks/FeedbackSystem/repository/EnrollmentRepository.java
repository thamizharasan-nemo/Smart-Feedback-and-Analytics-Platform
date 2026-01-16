package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    boolean existsByCourseAndStudent(Course course, User student);

    @Query("""
       SELECT CASE WHEN EXISTS (
           SELECT 1 FROM Enrollment e 
           JOIN e.student s
           JOIN e.course c 
           WHERE s.userId = :studentId AND 
                 c.courseId = :courseId
       ) THEN true ELSE false END
    """)
    boolean existsByCourseIdAndStudentId(@Param("courseId") Integer courseId,
                                         @Param("studentId") Integer studentId);

    List<Enrollment> findByStudent(User student);

    Enrollment findEnrollmentByStudentUserIdAndCourseCourseId(int studentId, int courseId);

    List<Enrollment> findByStudent_IdentityNo(String identityNo);

    @Query("SELECT COUNT(e) " +
            "FROM Enrollment e " +
            "LEFT JOIN e.course c " +
            "WHERE c.courseId = :courseId ")
    Integer getCourseEnrollmentCount(@Param("userId") int courseId);

    @Query("SELECT COUNT(e) " +
            "FROM Enrollment e " +
            "LEFT JOIN e.student s " +
            "WHERE s.userId = :studentId ")
    Integer getStudentEnrollmentCount(@Param("studentId") int studentId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.userId = :userId")
    Integer countStudentTotalEnrollments(@Param("userId") int userId);

}

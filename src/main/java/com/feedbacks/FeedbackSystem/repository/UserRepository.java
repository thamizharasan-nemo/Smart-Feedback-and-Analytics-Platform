package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.feedbacks.FeedbackSystem.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>,
        JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.identityNo = :identityNo")
    Optional<User> findByRollNo(String identityNo);

    // <> exclude the userId that passed, checks email with remaining users while updating
    boolean existsByEmailAndUserIdNot(String email, int userId);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = STUDENT")
    Integer totalStudentsCount();

    // Top Students by Participation (students who give the most feedback).
    @Query("SELECT u.userId AS studentId, " +
            "u.username AS studentName, " +
            "u.identityNo AS identityNo, " +
            "COUNT(f) AS feedbackCount " +
            "FROM User u LEFT JOIN Feedback f " +
            "ON u.userId = f.student.userId " +
            "WHERE u.role = STUDENT " +
            "GROUP BY u.userId, u.username " +
            "ORDER BY feedbackCount DESC")
    List<TopRatedStudentsDTO> findTopStudentsByFeedbacks(Pageable pageable);
}

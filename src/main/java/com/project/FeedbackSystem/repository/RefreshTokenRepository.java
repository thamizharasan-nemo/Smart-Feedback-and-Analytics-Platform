package com.example.FeedbackSystem.repository;

import com.example.FeedbackSystem.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    // Modifying because @query annotation use reading operation as default, so @Modifying is used
    // Transactional because either the operation should be completed fully or nothing should happen (to achieve ACID property)

    @Modifying
    @Transactional
    void deleteByUser_userId(int userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rf WHERE rf.expiresAt < :currentDate")
    void deleteByExpiryDateBefore(@Param("currentDate") LocalDate currentDate);
}

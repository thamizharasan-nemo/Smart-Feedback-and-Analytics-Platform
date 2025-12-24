package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.RefreshToken;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.RefreshTokenRepository;
import com.example.FeedbackSystem.security.SecureRandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class RefreshTokenService {

    @Autowired
    RefreshTokenRepository refreshTokenRepo;

    @Value("${jwt.refreshExpirationInSec}")
    private long refreshDays; // 10 days

    public RefreshToken issue(User user){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user); // set the user
        refreshToken.setToken(SecureRandomString.generateToken(64)); // generate refresh token for that user
        refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshDays)); // set expiration time for that token
        return refreshTokenRepo.save(refreshToken); //token saved in the database
    }

    public RefreshToken verifyUsable(String token){
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() ->new ResourceNotFoundException("Invalid Refresh token"));

        if(refreshToken.isRevoked())
            throw new IllegalStateException("Refresh token revoked");

        //expiration date is less than the current date, so expired
        if(refreshToken.getExpiresAt().isBefore(Instant.now()))
            throw new IllegalStateException("Refresh token expired");

        return refreshToken;
    }

    public RefreshToken rotate(RefreshToken current){
        //there is only one valid refresh token per user/session at a time.
        current.setRevoked(true);
        current.setReplacedAt(Instant.now());
        refreshTokenRepo.save(current);
        return issue(current.getUser());
    }

    public void revokeToken(String token) {
        refreshTokenRepo.findByToken(token)
                .ifPresent(rt -> {
                            rt.setRevoked(true);
                            refreshTokenRepo.save(rt);
                        }
                );
    }

    public void revokeAllForUser(Integer userId){
        refreshTokenRepo.deleteByUser_userId(userId);
    }

    @Transactional
    public void deleteExpiredTokens(){
        refreshTokenRepo.deleteByExpiryDateBefore(LocalDate.now());
    }

}

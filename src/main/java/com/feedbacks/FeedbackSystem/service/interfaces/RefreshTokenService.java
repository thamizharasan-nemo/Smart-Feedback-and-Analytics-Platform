package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.model.RefreshToken;
import com.feedbacks.FeedbackSystem.model.User;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenService {
    RefreshToken issue(User user);

    RefreshToken verifyUsable(String token);

    RefreshToken rotate(RefreshToken current);

    void revokeToken(String token);

    void revokeAllForUser(Integer userId);

    @Transactional
    void deleteExpiredTokens();
}

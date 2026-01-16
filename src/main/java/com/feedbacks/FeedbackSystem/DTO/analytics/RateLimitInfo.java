package com.feedbacks.FeedbackSystem.DTO.analytics;

import java.time.LocalDateTime;

public record RateLimitInfo(
        Integer limit,
        Integer remaining,
        String resetAt
) {}

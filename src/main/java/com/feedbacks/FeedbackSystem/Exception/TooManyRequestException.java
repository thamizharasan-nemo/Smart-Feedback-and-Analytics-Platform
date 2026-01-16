package com.feedbacks.FeedbackSystem.Exception;

import com.feedbacks.FeedbackSystem.DTO.analytics.RateLimitInfo;
import lombok.Getter;

@Getter
public class TooManyRequestException extends RuntimeException{

    private final RateLimitInfo rateLimitInfo;

    public TooManyRequestException(String message, RateLimitInfo rateLimitInfo){
        super(message);
        this.rateLimitInfo = rateLimitInfo;
    }
}

package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.analytics.RateLimitInfo;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.TooManyRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FeedbackRateLimiterService {

    private static final int DAILY_LIMIT = 10;

    private final StringRedisTemplate redisTemplate;

    public FeedbackRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public RateLimitInfo checkRateLimit(Integer studentId){
        String key = buildKey(studentId);

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1){
            redisTemplate.expire(key, secondsTillMidnight(), TimeUnit.SECONDS);
        }

        Integer remaining = (int)(DAILY_LIMIT - count);
        String restAt = LocalDate.now().plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (count > DAILY_LIMIT) {

            log.info(
                    "event=RATE_LIMITED userId={} ",
                    studentId
            );

            throw new TooManyRequestException(
                    "You've reached your limit of feedbacks. Try after "
                            + LocalDate.now().plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " .",
                    new RateLimitInfo(
                            DAILY_LIMIT,
                            remaining,
                            restAt
                    )
            );
        }

        return new RateLimitInfo(DAILY_LIMIT, remaining, restAt);
    }


    public String buildKey(Integer studentId){
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "feedback:rate:"+studentId+":today:"+date;
    }

    public Long secondsTillMidnight(){
        return Duration.between(
                LocalDateTime.now(),
                LocalDate.now().plusDays(1).atStartOfDay()
        ).getSeconds();
    }
}

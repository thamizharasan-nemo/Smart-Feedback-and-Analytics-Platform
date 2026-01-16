package com.feedbacks.FeedbackSystem.configure;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class FeedbackMetrics {

    private final MeterRegistry meterRegistry;

    public FeedbackMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementFeedbackSubmittedCount(){
        meterRegistry.counter("feedback.submitted.count").increment();
    }
}

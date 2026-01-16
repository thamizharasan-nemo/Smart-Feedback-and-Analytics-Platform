package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.analytics.CourseRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import com.feedbacks.FeedbackSystem.service.serviceImple.CourseServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.FeedbackServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.InstructorServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final FeedbackServiceImpl feedbackService;
    private final CourseServiceImpl courseService;
    private final InstructorServiceImpl instructorService;

    public AnalyticsController(FeedbackServiceImpl feedbackService, CourseServiceImpl courseService, InstructorServiceImpl instructorService) {
        this.feedbackService = feedbackService;
        this.courseService = courseService;
        this.instructorService = instructorService;
    }

    @GetMapping("/feedbacks/trends")
    public ResponseEntity<List<FeedbackTrendDTO>> feedbackTrends(@RequestParam String groupBy){
        return ResponseEntity.ok(feedbackService.getFeedbackTrends(groupBy));
    }

    @GetMapping("/feedbacks/ratings")
    public ResponseEntity<List<RatingDistributionDTO>> feedbackRatings(){
        return ResponseEntity.ok(feedbackService.getFeedbackRatings());
    }

    @GetMapping("/courses/top")
    public ResponseEntity<List<CourseRankingDTO>> courseRanking(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size){
        return ResponseEntity.ok(courseService.getCourseRanking(page, size));
    }

    @GetMapping("/instructors/top")
    public ResponseEntity<List<InstructorRankingDTO>> getTopRatedInstructor(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "5") int size){
        return ResponseEntity.ok(instructorService.getTopRatedInstructor(page, size));
    }

    @GetMapping("/instructors/rating/7")
    public ResponseEntity<Double> avgRatingOfInstructorsLast7days(){
        return ResponseEntity.ok(feedbackService.avgRatingOfInstructorsLast7days());
    }

    @GetMapping("/courses/rating/7")
    public ResponseEntity<Double> avgRatingOfCoursesLast7days(){
        return ResponseEntity.ok(feedbackService.avgRatingOfCoursesLast7days());
    }

    @GetMapping("/instructors/distributions/{instructorId}")
    public ResponseEntity<List<Object[]>> instructorsRatingDistributions(@PathVariable Integer instructorId){
        return ResponseEntity.ok(feedbackService.instructorRatingDistribution(instructorId));
    }
}

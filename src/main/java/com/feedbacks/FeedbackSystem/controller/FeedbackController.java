package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.service.serviceImple.FeedbackServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000/")
@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {

    private final FeedbackServiceImpl feedbackService;

    public FeedbackController(FeedbackServiceImpl feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacks() {
        return ResponseEntity.ok(feedbackService.getFeedbacks());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> createFeedback(@Valid @RequestBody FeedbackRequestDTO feedbackRequestDTO) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        HttpStatus.CREATED.toString(),
                        feedbackService.submitFeedback(feedbackRequestDTO)
                )
        );
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> editFeedback(@PathVariable Integer feedbackId,
                                                                         @RequestBody FeedbackRequestDTO feedbackRequestDTO) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        HttpStatus.ACCEPTED.toString(),
                        feedbackService.editFeedback(feedbackId, feedbackRequestDTO)
                )
        );
    }

    @DeleteMapping("/{feedbackId}/{userId}")
    public ResponseEntity<String> deleteFeedbackById(@PathVariable Integer feedbackId,
                                                     @PathVariable Integer userId) {
        feedbackService.deleteFeedbackById(userId, feedbackId);
        return ResponseEntity.ok().body("Feedback deleted successfully");
    }

    @DeleteMapping("/{feedbackId}/deleted/{userId}")
    public ResponseEntity<String> deleteFeedbackPermanentlyById(@PathVariable int userId,
                                                                @PathVariable int feedbackId,
                                                                @RequestParam(defaultValue = "true") boolean permanent){
        feedbackService.deleteFeedbackPermanently(userId, feedbackId);
        return ResponseEntity.ok().body("Course with ID: "+feedbackId+" deleted successfully");
    }

    @GetMapping("/soft/deleted")
    public ResponseEntity<List<FeedbackResponseDTO>> AllDeletedFeedback(@RequestParam(defaultValue = "true") boolean deleted){
        return ResponseEntity.ok(feedbackService.findAllSoftDeletedFeedbacks());
    }

    @PutMapping("/{feedbackId}/restore/{userId}")
    public ResponseEntity<FeedbackResponseDTO> restoreFeedback(@PathVariable int userId,
                                                               @PathVariable int feedbackId){
        return ResponseEntity.ok(feedbackService.restoreFeedback(userId, feedbackId));
    }




    @GetMapping("/{courseId}/sort")
    public ResponseEntity<Page<FeedbackResponseDTO>> getSortedFeedbackByCourseId(@PathVariable Integer courseId,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size,
                                                                                 @RequestParam(defaultValue = "submittedAt,DESC") String sort) {

        return ResponseEntity.ok(feedbackService.getSortedFeedbackByCourseId(courseId, page, size, sort));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FeedbackResponseDTO>> searchingCriteria(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String studentName,
            @RequestParam(value = "anonymous", required = false) Boolean anonymous,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(feedbackService.searchFeedback(courseId, studentId, minRating, keyword, studentName, anonymous, fromDate, toDate));
    }

    @GetMapping("/filter")
    public List<FeedbackResponseDTO> filterFeedback(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Boolean anonymous) {
        return feedbackService.getFilteredFeedback(courseId, minRating, maxRating, fromDate, toDate, anonymous);
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<Page<FeedbackResponseDTO>> getFeedbacksByUserId(@PathVariable Integer userId,
                                                                          @RequestParam(defaultValue = "0") int pageNo,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "submittedAt,DESC") String sort) {

        return ResponseEntity.ok(feedbackService.getFeedbackByUserId(userId, pageNo, size, sort));
    }

    @GetMapping("/courses/average/rating")
    public ResponseEntity<?> getAvgRatingForCourse(@RequestParam Integer courseId) {
        return ResponseEntity.ok(feedbackService.getAverageRatingForCourse(courseId));
    }

    @GetMapping("/instructors/average/rating")
    public ResponseEntity<?> getAvgRatingForInstructor(@RequestParam Integer instructorId) {
        return ResponseEntity.ok(feedbackService.getAverageRatingForInstructor(instructorId));
    }

    @GetMapping("courses/summary")
    public List<CourseFeedbackSummary> getCourseSummaries() {
        return feedbackService.getCourseSummary();
    }

    @GetMapping("students/{studentId}/courses/{courseId}")
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacksByStudentAndCourse(@PathVariable Integer studentId,
                                                                                    @PathVariable Integer courseId
    ) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByStudentAndCourse(studentId, courseId));
    }

    @GetMapping("/recent/{courseId}")
    public ResponseEntity<List<FeedbackResponseDTO>> getRecentFeedbacksByCourseId(@PathVariable Integer courseId) {
        return ResponseEntity.ok(feedbackService.getRecentFeedbacksByCourseId(courseId));
    }

}

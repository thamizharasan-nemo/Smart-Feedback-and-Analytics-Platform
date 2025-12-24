package com.example.FeedbackSystem.controller;

import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.example.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.example.FeedbackSystem.service.FeedbackService;
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
@RequestMapping("/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacks(){
        return ResponseEntity.ok(feedbackService.getFeedbacks());
    }

    @PostMapping("/create")
    public ResponseEntity<FeedbackResponseDTO> createFeedback(@Valid @RequestBody FeedbackRequestDTO feedbackRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.submitFeedback(feedbackRequestDTO));
    }

    @PutMapping("/edit/{feedbackDTOId}")
    public ResponseEntity<FeedbackResponseDTO> editFeedback(@PathVariable int feedbackDTOId, @RequestBody FeedbackRequestDTO feedbackRequestDTO){
        return ResponseEntity.ok(feedbackService.editFeedback(feedbackDTOId, feedbackRequestDTO));
    }

    @DeleteMapping("/delete/{feedbackId}")
    public ResponseEntity<?> deleteFeedbackById(@PathVariable int feedbackId){
            feedbackService.deleteFeedbackById(feedbackId);
            return ResponseEntity.ok().body("Feedback deleted successfully");
    }

    @GetMapping("/for-course/{courseId}/sort-by")
    public ResponseEntity<?> getFeedbackByCourseSorted(@PathVariable int courseId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(defaultValue ="submittedAt,DESC") String sort){

        return ResponseEntity.ok(feedbackService.getSortedFeedbackByCourseId(courseId, page, size, sort));
    }

    @GetMapping("/filter")
    public List<FeedbackResponseDTO> filterFeedback(
                                         @RequestParam(required = false) Integer courseId,
                                         @RequestParam(required = false) Integer minRating,
                                         @RequestParam(required = false) Integer maxRating,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                         @RequestParam(required = false) Boolean anonymous){
        return feedbackService.getFilteredFeedback(courseId, minRating, maxRating, fromDate, toDate, anonymous);
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<Page<FeedbackResponseDTO>> getFeedbacksByUserId(@PathVariable int userId,
                                                                          @RequestParam(defaultValue = "0") int pageNo,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "submittedAt,DESC") String sort){

        return ResponseEntity.ok(feedbackService.getFeedbackByUserId(userId, pageNo, size, sort));
    }

    @GetMapping("/course/averageRating")
    public ResponseEntity<?> getAvgRatingForCourse(@RequestParam int courseId) {
        return ResponseEntity.ok(feedbackService.getAverageRatingForCourse(courseId));
    }

    @GetMapping("/instructor/averageRating")
    public ResponseEntity<?> getAvgRatingForInstructor(@RequestParam int instructorId) {
        return ResponseEntity.ok(feedbackService.getAverageRatingForInstructor(instructorId));
    }

    @GetMapping("courses/summaries")
    public List<CourseFeedbackSummary> getCourseSummaries(){
        return feedbackService.getCourseSummary();
    }

    @GetMapping("by-student/{studentId}/and-course/{courseId}")
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacksByStudentAndCourse(
            @PathVariable int studentId,
            @PathVariable int courseId
    ) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByStudentAndCourse(studentId, courseId));
    }

    @GetMapping("/recent/feedbacks/{courseId}")
    public ResponseEntity<List<FeedbackResponseDTO>> getRecentFeedbacksByCourseId(@PathVariable int courseId){
        return ResponseEntity.ok(feedbackService.getRecentFeedbacksByCourseId(courseId));
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
}

package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.CourseRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackCountDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.PopularCourseDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.service.serviceImple.CourseServiceImpl;
import com.feedbacks.FeedbackSystem.service.other_services.HtmlEmailBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseServiceImpl courseService;
    private final HtmlEmailBody emailBody;

    public CourseController(CourseServiceImpl courseService, HtmlEmailBody emailBody) {
        this.courseService = courseService;
        this.emailBody = emailBody;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<Course>> getAllCourses(){
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/all")
    public ResponseEntity<List<CourseResponseDTO>> getAllCoursesDTO(){
        return ResponseEntity.ok(courseService.getAllCoursesDTO());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/id/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable int courseId){
            return ResponseEntity.ok(courseService.getCourseById(courseId));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseResponseById(@PathVariable int courseId){
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Course retrieved successfully",
                        courseService.getCourseResponseById(courseId)
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponseDTO>> addCourse(@RequestBody CourseRequestDTO requestDTO) {
        emailBody.newCourseAddedHtmlBody(requestDTO.getCourseName());
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Course added successfully",
                        courseService.addCourse(requestDTO)
                )
        );
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> updateCourseById(@PathVariable int courseId,
                                                              @RequestBody CourseRequestDTO requestDTO){
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Course updated successfully",
                        courseService.updateCourseById(courseId, requestDTO)
                )
        );
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<String>> deleteCourseById(@PathVariable int courseId){
            courseService.deleteCourseById(courseId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true,
                            "Course is soft deleted",
                            "Course is soft deleted successfully")
            );
    }

    @DeleteMapping("/{courseId}/deleted")
    public ResponseEntity<ApiResponse<String>> deleteCoursePermanentlyById(@PathVariable int courseId,
                                                              @RequestParam(defaultValue = "true") boolean permanent){
        courseService.deleteCoursePermanently(courseId);
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Permanently deleted",
                        "Course with ID: "+courseId+" deleted successfully")
        );
    }

    @GetMapping("/soft/deleted")
    public ResponseEntity<List<CourseResponseDTO>> AllDeletedCourses(@RequestParam(defaultValue = "true") boolean deleted){
        return ResponseEntity.ok(courseService.findAllSoftDeletedCourses());
    }

    @PutMapping("/{courseId}/restore")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> restoreCourse(@PathVariable int courseId){
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Course restored successfully",
                        courseService.restoreCourse(courseId)
                )
        );
    }

    @PutMapping("/{courseId}/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> assignCourse(@PathVariable int courseId,
                                                          @PathVariable int instructorId){
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Instructor assigned successfully",
                courseService.assignInstructorToCourse(courseId, instructorId)
        )
        );
    }

    @PutMapping("/{courseId}/instructor")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> unassignCourse(@PathVariable int courseId){
        return ResponseEntity.ok(new ApiResponse<>(
                        true,
                        "Instructor assigned successfully",
                        courseService.unassignInstructorToCourse(courseId)
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CourseResponseDTO>> getCoursesScalable(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Integer instructorId,
            @RequestParam(required = false) String instructorName,
            @RequestParam(required = false) Boolean popular,
            @RequestParam(required = false) Integer minEnrollments,
            @RequestParam(required = false, defaultValue = "2.5") Double minAvgRating,
            @RequestParam(defaultValue = "courseId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                courseService.getCoursesScalable(
                        courseId, courseName,
                        instructorId, instructorName,
                        popular, minEnrollments, minAvgRating,
                        sortBy, sortDirection, page, size
                )
        );
    }

    @GetMapping("/param")
    public ResponseEntity<List<CourseResponseDTO>> searchCourse(@RequestParam(required = false) Integer courseId,
                                                                @RequestParam(required = false) Integer instructorId,
                                                                @RequestParam(required = false) String courseName,
                                                                @RequestParam(required = false) String instructorName
    ){
        return ResponseEntity.ok(courseService.searchCourse(courseId, instructorId, courseName, instructorName));
    }

    @GetMapping("/name")
    public ResponseEntity<List<CourseResponseDTO>> searchCourseByName(@RequestParam String courseName){
        return ResponseEntity.ok(courseService.searchCourseByName(courseName));
    }

    //get all courses that a single instructor teach
    @GetMapping("/instructors/{instructorId}/courses")
    public ResponseEntity<List<CourseResponseDTO>> getCoursesForInstructor(@RequestParam int instructorId) {
        return ResponseEntity.ok(courseService.getCoursesForInstructor(instructorId));

    }

    @GetMapping("/counts/ratings")
    public ResponseEntity<List<CourseFeedbackCountDTO>> getFeedbackCountAndAvgRating(){
        return ResponseEntity.ok(courseService.getFeedbackCountAndAvg());
    }

    @GetMapping("/order")
    public ResponseEntity<List<CourseResponseDTO>> getSortedCourse(
            @RequestParam(defaultValue = "ASC") String sortBy) {
        return ResponseEntity.ok(courseService.getSortedCourses(sortBy));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PopularCourseDTO>> getPopularCourses(@RequestParam(defaultValue = "0") int pageNumber,
                                                                    @RequestParam(defaultValue = "10") int pageSize){
        return ResponseEntity.ok(courseService.getPopularCourses(pageNumber, pageSize));
    }

    @GetMapping("/unpopular")
    public ResponseEntity<Slice<PopularCourseDTO>> getUnPopularCourses(@RequestParam(defaultValue = "0") int pageNumber,
                                                                       @RequestParam(defaultValue = "10") int pageSize){
        return ResponseEntity.ok(courseService.getUnPopularCourses(pageNumber, pageSize));
    }

    @GetMapping("/enrollments/greater")
    public ResponseEntity<List<CourseResponseDTO>> getByEnrollmentsHigherThan(
                                                            @RequestParam(required = false) int minEnrollment){
        return ResponseEntity.ok(courseService.hasEnrollmentsGreaterThan(minEnrollment));
    }

    // Courses with no feedbacks using specification API
    @GetMapping("/feedbacks")
    public ResponseEntity<List<CourseResponseDTO>> getCoursesWithoutFeedbacks(@RequestParam("zero") boolean zero) {
        return ResponseEntity.ok(courseService.findCoursesWithoutFeedback());
    }

    @GetMapping("/instructor")
    public ResponseEntity<List<CourseResponseDTO>> getCoursesNotAssigned(
            @RequestParam(name = "assigned", defaultValue = "false") boolean assigned) {
        return ResponseEntity.ok(courseService.findCoursesNotAssigned());
    }

    @GetMapping("/average")
    public ResponseEntity<List<CourseResponseDTO>> getCoursesGreaterThanAvgRating(@RequestParam(defaultValue = "2.5") Double avgRating) {
        return ResponseEntity.ok(courseService.findCoursesLessThanAvgRating(avgRating));
    }
}

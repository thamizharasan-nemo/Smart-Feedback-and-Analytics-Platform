package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.CourseRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackCountDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.service.CourseService;
import com.feedbacks.FeedbackSystem.service.other_services.HtmlEmailBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final HtmlEmailBody emailBody;

    public CourseController(CourseService courseService, HtmlEmailBody emailBody) {
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
        return new ResponseEntity<>(courseService.getAllCoursesDTO(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/id/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable int courseId){
            return new ResponseEntity<>(courseService.getCourseById(courseId), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<CourseResponseDTO> addCourse(@RequestBody Course course) {
        emailBody.newCourseAddedHtmlBody(course.getCourseName());
        return ResponseEntity.ok(courseService.addCourse(course));
    }

    @PutMapping("/update/{courseId}")
    public ResponseEntity<CourseResponseDTO> updateCourseById(@PathVariable int courseId,
                                                              @RequestBody CourseRequestDTO requestDTO){
        return ResponseEntity.ok(courseService.updateCourseById(courseId, requestDTO));
    }

    @DeleteMapping("delete/{courseId}")
    public ResponseEntity<?> deleteCourseById(@PathVariable int courseId){
            courseService.deleteCourseById(courseId);
            return ResponseEntity.ok().body("Course is soft deleted successfully");
    }

    @GetMapping("/all/deleted")
    public ResponseEntity<List<CourseResponseDTO>> AllDeletedCourses(){
        return ResponseEntity.ok(courseService.findAllSoftDeletedCourses());
    }

    @PutMapping("/restore/{courseId}")
    public ResponseEntity<CourseResponseDTO> restoreCourse(@PathVariable int courseId){
        return ResponseEntity.ok(courseService.restoreCourse(courseId));
    }

    @DeleteMapping("delete/permanent/{courseId}")
    public ResponseEntity<?> deleteCoursePermanentlyById(@PathVariable int courseId){
        courseService.deleteCoursePermanently(courseId);
        return ResponseEntity.ok().body("Course with ID: "+courseId+" deleted successfully");
    }

    @PutMapping("/assign/{courseId}/toInstructor/{instructorId}")
    public ResponseEntity<CourseResponseDTO> assignCourse(@PathVariable int courseId,
                                                          @PathVariable int instructorId){
        return ResponseEntity.ok(courseService.assignInstructorToCourse(courseId, instructorId));
    }

    @PutMapping("/{courseId}/unassign-instructor")
    public ResponseEntity<CourseResponseDTO> unassignCourse(@PathVariable int courseId){
        return ResponseEntity.ok(courseService.unassignInstructorToCourse(courseId));
    }


    @GetMapping("/searchBy")
    public ResponseEntity<List<CourseResponseDTO>> searchCourseByName(@RequestParam String courseName){
        return ResponseEntity.ok(courseService.searchCourseByName(courseName));
    }

    //get all courses that a single instructor teach
    @GetMapping("/all/instructor/Id")
    public ResponseEntity<?> getCoursesForInstructor(@RequestParam int instructorId){
        return ResponseEntity.ok(courseService.getCoursesForInstructor(instructorId));

    }

    @GetMapping("/feedback-status")
    public ResponseEntity<List<CourseFeedbackCountDTO>> getFeedbackCountAndAvgRating(){
        return ResponseEntity.ok(courseService.getFeedbackCountAndAvg());
    }

    @GetMapping("/sort-by")
    public ResponseEntity<List<CourseResponseDTO>> getSortedCourse(
            @RequestParam(defaultValue = "ASC") String sortBy) {
        return ResponseEntity.ok(courseService.getSortedCourses(sortBy));
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularCourses(@RequestParam(required = false) int pageNumber,
                                               @RequestParam(defaultValue = "10") int pageSize){
        return ResponseEntity.ok(courseService.getPopularCourses(pageNumber, pageSize));
    }

    @GetMapping("/unpopular")
    public ResponseEntity<?> getUnPopularCourses(@RequestParam(required = false) int pageNumber,
                                               @RequestParam(defaultValue = "10") int pageSize){
        return ResponseEntity.ok(courseService.getUnPopularCourses(pageNumber, pageSize));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseResponseDTO>> searchCourse(@RequestParam(required = false) Integer courseId,
                                                                @RequestParam(required = false) Integer instructorId,
                                                                @RequestParam(required = false) String courseName,
                                                                @RequestParam(required = false) String instructorName
    ){
        return ResponseEntity.ok(courseService.searchCourse(courseId, instructorId, courseName, instructorName));
    }

    @GetMapping("/enrollments/greater")
    public ResponseEntity<?> getByEnrollmentsHigherThan(@RequestParam(required = false) int minEnrollment){
        return ResponseEntity.ok(courseService.hasEnrollmentsGreaterThan(minEnrollment));
    }

    // Courses with no feedbacks using specification API
    @GetMapping("/no-feedbacks")
    public ResponseEntity<List<CourseResponseDTO>> getCoursesWithoutFeedbacks() {
        return ResponseEntity.ok(courseService.findCoursesWithoutFeedback());
    }

    @GetMapping("/not-assigned")
    public ResponseEntity<List<CourseResponseDTO>> getCoursesNotAssigned() {
        return ResponseEntity.ok(courseService.findCoursesNotAssigned());
    }

    @GetMapping("/greater/avg-rating")
    public ResponseEntity<List<CourseResponseDTO>> getCoursesGreaterThanAvgRating() {
        return ResponseEntity.ok(courseService.findCoursesLessThanAvgRating());
    }
}

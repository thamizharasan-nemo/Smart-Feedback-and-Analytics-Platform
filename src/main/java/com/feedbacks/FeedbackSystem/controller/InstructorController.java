package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.service.serviceImple.InstructorServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instructors")
public class InstructorController {

    private final InstructorServiceImpl instructorService;

    public InstructorController(InstructorServiceImpl instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping
    public ResponseEntity<List<Instructor>> getAllInstructor(){
        return ResponseEntity.ok(instructorService.getAllInstructor());
    }

    @GetMapping("/admin/{instructorId}")
    public ResponseEntity<Instructor> getInstructorById(@PathVariable int instructorId){
        return ResponseEntity.ok(instructorService.getInstructorById(instructorId));
    }

    @GetMapping("/{instructorId}")
    public ResponseEntity<InstructorResponseDTO> getInstructorResponseById(@PathVariable Integer instructorId){
        return ResponseEntity.ok(instructorService.getInstructorResponseById(instructorId));
    }

    @PostMapping
    public ResponseEntity<InstructorResponseDTO> addInstructor(@RequestBody InstructorRequestDTO requestDTO){
        return ResponseEntity.ok(instructorService.addInstructor(requestDTO));
    }

    @PutMapping("/instructorId")
    public ResponseEntity<InstructorResponseDTO> updateInstructorById(@PathVariable Integer instructorId,
                                                                      @RequestBody InstructorRequestDTO requestDTO){
        return ResponseEntity.ok(instructorService.updateInstructorById(instructorId, requestDTO));
    }

    @DeleteMapping("/instructorId")
    public ResponseEntity<String> deleteInstructorById(@PathVariable Integer instructorId) {
        instructorService.deleteInstructorById(instructorId);
        return ResponseEntity.ok().body("Instructor with ID: " + instructorId + " deleted successfully");
    }

    @DeleteMapping("/{instructorId}/deleted")
    public ResponseEntity<String> deleteInstructorPermanentlyById(@PathVariable int instructorId,
                                                              @RequestParam(defaultValue = "true") boolean permanent){
        instructorService.deleteInstructorPermanently(instructorId);
        return ResponseEntity.ok().body("Instructor with ID: "+instructorId+" deleted successfully");
    }

    @GetMapping("/soft/deleted")
    public ResponseEntity<List<InstructorResponseDTO>> AllDeletedInstructor(@RequestParam(defaultValue = "true") boolean deleted){
        return ResponseEntity.ok(instructorService.findAllSoftDeletedInstructors());
    }

    @PutMapping("/{instructorId}/restore")
    public ResponseEntity<InstructorResponseDTO> restoreInstructor(@PathVariable int instructorId){
        return ResponseEntity.ok(instructorService.restoreInstructor(instructorId));
    }




    // Assigning and unassigning course to instructor
    @PutMapping("/course/{courseId}/instructor/{instructorId}")
    public ResponseEntity<InstructorResponseDTO> assignCourse(@PathVariable Integer instructorId,
                                          @PathVariable Integer courseId) {
        return ResponseEntity.ok(instructorService.assignCourseToInstructor(instructorId, courseId));
    }

    @DeleteMapping("/courses/{courseId}/instructor/{instructorId}")
    public ResponseEntity<String> unassignCourseFromInstructor(@PathVariable int instructorId, @PathVariable int courseId) {
        instructorService.unassignCourseFromInstructor(instructorId, courseId);
        return ResponseEntity.ok("Course unassigned from instructor");
    }

    @GetMapping("/unassigned/courses")
    public ResponseEntity<List<InstructorResponseDTO>> getAllUnassignedInstructors(){
        return ResponseEntity.ok(instructorService.getUnassignedInstructors());
    }

    @GetMapping("/assigned/courses/{instructorId}")
    public ResponseEntity<List<String>> viewAssignedCourses(@PathVariable int instructorId){
        return ResponseEntity.ok(instructorService.viewAssignedCourseForInstructor(instructorId));

    }

    @GetMapping("/feedbacks")
    public ResponseEntity<List<FeedbacksByInstructor>> getAllFeedbacksByInstructor(){
        return ResponseEntity.ok(instructorService.getAllFeedbacksByInstructor());
    }

    @GetMapping("/top")
    public ResponseEntity<List<TopRatedInstructorsDTO>> getAllTopRatedInstructors(){
        return ResponseEntity.ok(instructorService.getAllTopRatedInstructors());
    }

    @GetMapping("/search")
    public ResponseEntity<List<InstructorResponseDTO>> searchCourse(@RequestParam(required = false) Integer instructorId,
                                                                    @RequestParam(required = false) String instructorName,
                                                                    @RequestParam(required = false) String courseName
    ){
        return ResponseEntity.ok(instructorService.searchInstructor(instructorId, instructorName, courseName));
    }
}

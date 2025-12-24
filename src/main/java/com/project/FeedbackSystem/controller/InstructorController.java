package com.example.FeedbackSystem.controller;

import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.example.FeedbackSystem.model.Instructor;
import com.example.FeedbackSystem.service.InstructorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instructors")
public class InstructorController {

    @Autowired
    InstructorService instructorService;

    @GetMapping("/all")
    public ResponseEntity<List<Instructor>> getAllInstructor(){
        return ResponseEntity.ok(instructorService.getAllInstructor());
    }

    @GetMapping("/id/{instructorId}")
    public ResponseEntity<Instructor> getInstructorById(@PathVariable int instructorId){
        return ResponseEntity.ok(instructorService.getInstructorById(instructorId));
    }

    @GetMapping("/id/check/{instructorId}")
    public ResponseEntity<InstructorResponseDTO> getInstructorResponseById(@PathVariable int instructorId){
        return ResponseEntity.ok(instructorService.getInstructorResponseById(instructorId));
    }

    @PostMapping("/create")
    public ResponseEntity<InstructorResponseDTO> addInstructor(@RequestBody Instructor instructor){
        return ResponseEntity.ok(instructorService.addInstructor(instructor));
    }

    @PutMapping("/update/{instructorId}")
    public ResponseEntity<InstructorResponseDTO> updateInstructorById(@PathVariable int instructorId, @RequestBody InstructorRequestDTO requestDTO){
        return ResponseEntity.ok(instructorService.updateInstructorById(instructorId, requestDTO));
    }

    @DeleteMapping("delete/{instructorId}")
    public ResponseEntity<?> deleteInstructorById(@PathVariable int instructorId) {
        instructorService.deleteInstructorById(instructorId);
        return ResponseEntity.ok().body("Instructor with ID: " + instructorId + " deleted successfully");
    }


    // Assigning and unassigning course to instructor
    @PutMapping("/{instructorId}/assign-course/{courseId}")
    public ResponseEntity<?> assignCourse(@PathVariable int instructorId,
                                          @PathVariable int courseId) {
        return ResponseEntity.ok(instructorService.assignCourseToInstructor(instructorId, courseId));
    }

    @PutMapping("/{instructorId}/unassign-course/{courseId}")
    public ResponseEntity<String> unassignCourseFromInstructor(@PathVariable int instructorId, @PathVariable int courseId) {
        instructorService.unassignCourseFromInstructor(instructorId, courseId);
        return ResponseEntity.ok("Course unassigned from instructor");
    }

    @GetMapping("/all/unassigned-instructor")
    public ResponseEntity<?> getAllUnassignedInstructors(){
        return ResponseEntity.ok(instructorService.getUnassignedInstructors());
    }

    @GetMapping("/courses-assigned/{instructorId}")
    public ResponseEntity<List<?>> viewAssignedCourses(@PathVariable int instructorId){
        return ResponseEntity.ok(instructorService.viewAssignedCourseForInstructor(instructorId));

    }

    @GetMapping("/all-feedbacks/instructor")
    public ResponseEntity<List<?>> getAllFeedbacksByInstructor(){
        return ResponseEntity.ok(instructorService.getAllFeedbacksByInstructor());
    }

    @GetMapping("/top-rated/instructors")
    public ResponseEntity<List<?>> getAllTopRatedInstructors(){
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

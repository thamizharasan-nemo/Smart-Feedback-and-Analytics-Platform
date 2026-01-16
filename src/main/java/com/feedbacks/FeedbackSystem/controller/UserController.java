package com.feedbacks.FeedbackSystem.controller;


import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.service.serviceImple.UserServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void started(){
        log.info("🔥 VERSION CHECK: 2026-01-13 build-2");
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsersByRole(@RequestParam(defaultValue = "STUDENT") String role) {
        User.Role roleEnum = User.Role.valueOf(role.toUpperCase());
        return ResponseEntity.ok(userService.getAllUsersByRole(roleEnum));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsersAsResponseDTO(){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Fetched All users",
                        userService.getAllUsersAsResponseDTO()
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/admin/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserResponseById(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserResponseById(userId));
    }

    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<UserResponseDTO> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.addUser(userRequestDTO));
    }

    @PreAuthorize("permitAll()")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable int userId, @Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.updateUser(userId, userRequestDTO));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable int userId) {
        userService.deleteByUserId(userId);
        return ResponseEntity.ok("User deleted successfully.");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/rollno")
    public ResponseEntity<UserResponseDTO> findByRollNo(@RequestParam String rollNo) {
        return ResponseEntity.ok(userService.getByRollNo(rollNo));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/email")
    public ResponseEntity<UserResponseDTO> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    @GetMapping("/students/count")
    public ResponseEntity<Integer> countTotalStudents() {
        return ResponseEntity.ok(userService.getTotalStudentsCount());
    }

    @GetMapping("/students/top")
    public ResponseEntity<List<TopRatedStudentsDTO>> topRatedStudents(@RequestParam(defaultValue = "0") int pageNumber,
                                                                      @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(userService.getTopRatedStudents(pageNumber, limit));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchCourse(@RequestParam(required = false) Integer userId,
                                                              @RequestParam(required = false) Integer studentId,
                                                              @RequestParam(required = false) String studentName,
                                                              @RequestParam(required = false) String rollNo
                                                              ){
        return ResponseEntity.ok(userService.searchUser(userId, studentId, studentName, rollNo));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserResponseDTO>> searchAdmin(@RequestParam(required = false) Integer adminUId,
                                                              @RequestParam(required = false) String adminId) {
        return ResponseEntity.ok(userService.searchAdmin(adminUId, adminId));
    }

    @GetMapping("/students")
    public ResponseEntity<List<UserResponseDTO>> getStudents(@RequestParam(required = false, defaultValue = "false") Boolean hasFeedback,
                                                             @RequestParam(required = false, defaultValue = "false") Boolean enrolled,
                                                             @RequestParam(required = false, defaultValue = "0") Integer courseId) {
        return ResponseEntity.ok(userService.getStudents(hasFeedback, enrolled, courseId));
    }

    @GetMapping("/feedbacks/students")
    public ResponseEntity<List<UserResponseDTO>> getStudentsWithoutFeedbacks(@RequestParam(defaultValue = "false") Boolean hasFeedback) {
        return ResponseEntity.ok(userService.findStudentsWithoutFeedback());
    }

    @GetMapping("/unrolled/students")
    public ResponseEntity<List<UserResponseDTO>> getStudentsWithoutEnrollment(@RequestParam Boolean enrolled) {
        return ResponseEntity.ok(userService.findStudentsWithoutEnrollment());
    }

    @GetMapping("/students/course")
    public ResponseEntity<List<UserResponseDTO>> getStudentsEnrolledToThisCourse(@RequestParam Integer courseId) {
        return ResponseEntity.ok(userService.studentsEnrolledToThisCourse(courseId));
    }
}

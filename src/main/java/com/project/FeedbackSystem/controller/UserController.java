package com.example.FeedbackSystem.controller;


import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/all-by")
    public ResponseEntity<List<UserResponseDTO>> getAllUsersByRole(
            @RequestParam(defaultValue = "STUDENT") String role) {
        User.Role roleEnum = User.Role.valueOf(role.toUpperCase());
        return new ResponseEntity<>(userService.getAllUsersByRole(roleEnum), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/id/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/create")
    public ResponseEntity<?> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        return new ResponseEntity<>(userService.addUser(userRequestDTO), HttpStatus.OK);
    }

    @PreAuthorize("permitAll()")
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @Valid @RequestBody UserRequestDTO userRequestDTO) {
        return new ResponseEntity<>(userService.updateUser(userId, userRequestDTO), HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        userService.deleteByUserId(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/searchBy")
    public ResponseEntity<?> findByRollNo(@RequestParam String rollNo) {
        return ResponseEntity.ok(userService.getByRollNo(rollNo));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/by-email")
    public ResponseEntity<?> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    @GetMapping("/count/total-students")
    public ResponseEntity<?> countTotalStudents() {
        return ResponseEntity.ok(userService.getTotalStudentsCount());
    }

    @GetMapping("/top-students/page")
    public ResponseEntity<?> topRatedStudents(@RequestParam(required = false) int pageNumber,
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

    @GetMapping("/search/admin")
    public ResponseEntity<List<UserResponseDTO>> searchCourse(@RequestParam(required = false) Integer adminUId,
                                                              @RequestParam(required = false) String adminId) {
        return ResponseEntity.ok(userService.searchAdmin(adminUId, adminId));
    }

    @GetMapping("/students/no-feedbacks")
    public ResponseEntity<List<UserResponseDTO>> getStudentsWithoutFeedbacks() {
        return ResponseEntity.ok(userService.findStudentsWithoutFeedback());
    }

    @GetMapping("/students/without-enrollment")
    public ResponseEntity<List<UserResponseDTO>> getStudentsWithoutEnrollment() {
        return ResponseEntity.ok(userService.findStudentsWithoutEnrollment());
    }

    @GetMapping("/students/enrolled-to")
    public ResponseEntity<List<UserResponseDTO>> getStudentsEnrolledToThisCourse(@RequestParam Integer courseId) {
        return ResponseEntity.ok(userService.studentsEnrolledToThisCourse(courseId));
    }
}

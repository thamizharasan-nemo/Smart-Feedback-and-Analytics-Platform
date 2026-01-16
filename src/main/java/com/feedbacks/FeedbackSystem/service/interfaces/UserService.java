package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.feedbacks.FeedbackSystem.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(int userId);

    UserResponseDTO getUserResponseById(int userId);

    // Return all Users as DTO
    List<UserResponseDTO> getAllUsersAsResponseDTO();

    // Return all users by userId -- fetch all required data in one query - faster
    List<User> getAllUsersById();

    UserResponseDTO addUser(UserRequestDTO userRequestDTO);

    UserResponseDTO updateUser(int userId, UserRequestDTO userRequestDTO);

    void deleteByUserId(int userId);

    UserResponseDTO getByRollNo(String rollNo);

    List<UserResponseDTO> getAllUsersByRole(User.Role roleEnum);

    UserResponseDTO getByEmail(String email);

    Integer getTotalStudentsCount();

    List<TopRatedStudentsDTO> getTopRatedStudents(int pageNumber, int limit);

    List<UserResponseDTO> searchUser(Integer userId,
                                     Integer studentId,
                                     String studentName,
                                     String rollNo);

    // ADMIN search specifications
    List<UserResponseDTO> searchAdmin(Integer adminUId,
                                      String adminId);

    // Single method to return any of the below methods
    List<UserResponseDTO> getStudents(Boolean hasFeedbacks,
                                      Boolean enrolled,
                                      Integer courseId);

    List<UserResponseDTO> findStudentsWithoutFeedback();

    List<UserResponseDTO> findStudentsWithFeedback();

    List<UserResponseDTO> findStudentsWithoutEnrollment();

    List<UserResponseDTO> studentsEnrolledToThisCourse(Integer courseId);
}

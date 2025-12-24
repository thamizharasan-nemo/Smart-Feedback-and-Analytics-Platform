package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.example.FeedbackSystem.Exception.BadRequestException;
import com.example.FeedbackSystem.Exception.NotAllowedException;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.mapper.UserMapper;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.UserRepository;
import com.example.FeedbackSystem.specification.UserSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepo, UserMapper userMapper) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(int userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    // Return all Users as DTO
    public List<UserResponseDTO> getAllUsersAsDTO(){
        return userRepo.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
    // Return all users by userId -- fetch all required data in one query - faster
    public List<User> getAllUsersById(){
        return userRepo.findAllById(getAllUsersAsDTO().stream().
                map(UserResponseDTO::getUserId)
                .toList());
    }


    public User addUser(UserRequestDTO userRequestDTO) {
        Optional<User> isExist = userRepo.findByEmail(userRequestDTO.getEmail());

        if(isExist.isPresent()){
            throw new BadRequestException("This email already exists.");
        }

        if(userRequestDTO.getRole() == User.Role.STUDENT){
            System.out.println(userRequestDTO.getRollNo());
            if(userRequestDTO.getRollNo() == null)
                throw new BadRequestException("Invalid roll number");
        }

        if(userRequestDTO.getRole().equals(User.Role.ADMIN)){
            if(userRequestDTO.getAdminId() == null){
                throw new NotAllowedException("Admin id is required");
            }
        }

        User user = new User();
        return userRepo.save(userMapper.toEntity(user, userRequestDTO));
    }


    public User updateUser(int userId, UserRequestDTO userRequestDTO) {
        if(!userRepo.existsById(userId)){
            throw new ResourceNotFoundException("User not found! Id: "+userId);
        }

        if(userRepo.existsByEmailAndUserIdNot(userRequestDTO.getEmail(), userId)) {
            throw new BadRequestException("Email already exists.");
        }

        if(userRequestDTO.getRole().equals(User.Role.ADMIN)){
            if(userRequestDTO.getAdminId() == null){
                throw new NotAllowedException("Admin id is required");
            }
        }
        User userExist = getUserById(userId);

        return userRepo.save(userMapper.toEntity(userExist, userRequestDTO));
    }

    public void deleteByUserId(int userId) {
        if(!userRepo.existsById(userId)){
            throw new ResourceNotFoundException("User not exist - ID:"+userId);
        }

        userRepo.deleteById(userId);
    }

    public UserResponseDTO getByRollNo(String rollNo){
        User user = userRepo.findByRollNo(rollNo)
                .orElseThrow(() ->new ResourceNotFoundException("Student with rollNo "+rollNo+" not found."));
        return userMapper.toResponse(user);
    }

    public List<UserResponseDTO> getAllUsersByRole(User.Role roleEnum){
        return userRepo.findByRole(roleEnum).stream()
                .map(user -> new UserResponseDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getIdentityNo(),
                        user.getUserCreatedAt().toString())
                ).collect(Collectors.toList());
    }

    public UserResponseDTO getByEmail(String email){
        User student = userRepo.findByEmail(email)
                .orElseThrow(() ->new ResourceNotFoundException("Student with email " + email + " not found."));
        return userMapper.toResponse(student);
    }

    public Integer getTotalStudentsCount() {
        return userRepo.totalStudentsCount();
    }

    public List<TopRatedStudentsDTO> getTopRatedStudents(int pageNumber, int limit) {
        Pageable pageable = PageRequest.of(pageNumber, limit);
        return userRepo.findTopStudentsByFeedbacks(pageable);
    }

    public List<UserResponseDTO> searchUser(Integer userId,
                                            Integer studentId,
                                            String studentName,
                                            String rollNo){
        Specification<User> specification = Specification.allOf(
                UserSpecification.hasUserId(userId),
                UserSpecification.studentById(studentId),
                UserSpecification.hasStudentName(studentName),
                UserSpecification.hasRollNo(rollNo)
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // ADMIN search specifications
    public List<UserResponseDTO> searchAdmin(Integer adminUId,
                                            String adminId) {
        Specification<User> specification = Specification.allOf(
                UserSpecification.adminById(adminUId),
                UserSpecification.hasAdminIdNo(adminId)
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public List<UserResponseDTO> findStudentsWithoutFeedback(){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsWithoutFeedback()
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public List<UserResponseDTO> findStudentsWithoutEnrollment(){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsWithoutEnrollments()
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public List<UserResponseDTO> studentsEnrolledToThisCourse(Integer courseId){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsEnrolledToThisCourse(courseId)
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
